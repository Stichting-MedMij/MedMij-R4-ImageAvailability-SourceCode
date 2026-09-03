// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.medmij.pgo.domain.models.dicom.Instance;
import nl.medmij.pgo.domain.models.dicom.Metadata;
import nl.medmij.pgo.domain.models.dicom.SopClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.UUID;

/**
 * Abstract base class to be implemented by clients of this library to perform the actual download
 * of the DICOM objects.
 */
public abstract class WadoRsDownloader {
	public static final class Response {
		public final String contentType;
		public final int status;
		public final InputStream contents;

		public Response(String contentType, int status, InputStream contents) {
			this.contentType = contentType;
			this.status = status;
			this.contents = contents;
		}
	}

	public static final class SopClassNotSupportedException extends Exception {
		public final SopClass sopClass;
		private SopClassNotSupportedException(SopClass sopClass) {
			this.sopClass = sopClass;
		}
	}

	public Response doDownload(String bearer, UUID xCorrelatioId, Instance instance) throws IOException, SopClassNotSupportedException {
		SopClass sopClass = instance.getSopClass();
		if (SopClass.STRUCTURED_REPORTS.contains(sopClass)) {
			throw new SopClassNotSupportedException(sopClass);
		} else if (SopClass.MULTI_FRAME_IMAGES.contains(sopClass) || SopClass.VIDEO.contains(sopClass)) {
			return doDownloadMultiFrame(bearer, xCorrelatioId, instance);
		} else if (SopClass.PDF.contains(sopClass)) {
			return doDownloadEncapsulatedDocument(bearer, xCorrelatioId, instance);
		}

		URI url = WadoRsBuilder.fromInstance(instance).endpoint(WadoRsBuilder.Endpoint.RENDERED).generateInstancesUrl();
		String accept = "image/jpeg";
		return doRequest(url, bearer, accept, xCorrelatioId, UUID.randomUUID());
	}

	public Response doDownloadFrame(String bearer, UUID xCorrelatioId, Instance instance, int frameNumber) throws IOException, SopClassNotSupportedException {
		SopClass sopClass = instance.getSopClass();
		if (SopClass.MULTI_FRAME_IMAGES.contains(sopClass) || SopClass.VIDEO.contains(sopClass)) {
			URI url = WadoRsBuilder.fromInstance(instance).frameNumber("" + frameNumber).endpoint(WadoRsBuilder.Endpoint.RENDERED).generateFramesUrl();
			String accept = "image/jpeg";
			return doRequest(url, bearer, accept, xCorrelatioId, UUID.randomUUID());
		}
		throw new SopClassNotSupportedException(sopClass);
	}

	public Response doDownloadDicom(String bearer, UUID xCorrelatioId, Instance instance) throws IOException {
		URI url = WadoRsBuilder.fromInstance(instance).generateInstancesUrl();
		String accept = "application/dicom";
		return doRequest(url, bearer, accept, xCorrelatioId, UUID.randomUUID());
	}

	private Response doDownloadEncapsulatedDocument(String bearer, UUID xCorrelatioId, Instance instance) throws IOException {
		Response resp = doDownloadMetadata(bearer, xCorrelatioId, instance);
		Metadata metadata = mapResponseToMetadata(resp);
		if (metadata.hasData()) {
			return new Response(metadata.getMimeType(), resp.status, new ByteArrayInputStream(Base64.getDecoder().decode(metadata.getData())));
		}
		if (metadata.hasBulkDataUri()) {
			return doDownloadRenderedDocument(bearer, xCorrelatioId, instance, metadata);
		}
		return new Response(metadata.getMimeType(), 204 /* no content */, new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * Downloads an encapsulated document (e.g. a PDF) via the WADO-RS {@code /rendered} sub-resource on the
	 * instance, rather than following {@code BulkDataURI} directly. The instance-level {@code /rendered} sub-resource
	 * is the more broadly supported route and, per the DICOM standard, returns the document's native representation
	 * (e.g. {@code application/pdf}) by default for this SOP class. Here, the presence of {@code BulkDataURI} in
	 * the metadata is used only as the signal that the document isn't inlined and must be retrieved separately;
	 * the URI value itself is not used.
	 */
	private Response doDownloadRenderedDocument(String bearer, UUID xCorrelatioId, Instance instance, Metadata metadata) throws IOException {
		URI url = WadoRsBuilder.fromInstance(instance).endpoint(WadoRsBuilder.Endpoint.RENDERED).generateInstancesUrl();
		String accept = metadata.getMimeType().isEmpty() ? "application/octet-stream" : metadata.getMimeType();
		Response resp = doRequestAndHandleInvalidStatus(url, bearer, accept, xCorrelatioId);
		return new Response(metadata.getMimeType(), resp.status, resp.contents);
	}

	private Response doDownloadMultiFrame(String bearer, UUID xCorrelatioId, Instance instance) throws IOException {
		Response resp = doDownloadMetadata(bearer, xCorrelatioId, instance);
		Metadata metadata = mapResponseToMetadata(resp);
		String jsonResp = String.format("{\"numberOfFrames\": %d}", metadata.getNumberOfFrames());
		return new Response("application/json", resp.status, new ByteArrayInputStream(jsonResp.getBytes()));
	}

	private Response doDownloadMetadata(String bearer, UUID xCorrelatioId, Instance instance) throws IOException {
		URI url = WadoRsBuilder.fromInstance(instance).endpoint(WadoRsBuilder.Endpoint.METADATA).generateInstancesUrl();
		String accept = "application/dicom+json";
		return doRequestAndHandleInvalidStatus(url, bearer, accept, xCorrelatioId);
	}

	private Metadata mapResponseToMetadata(Response resp) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode metadataArray = mapper.readTree(resp.contents);
		JsonNode metadataObject = metadataArray.get(0);
		if (metadataObject == null) {
			throw new IOException("Did not receive proper metadata response");
		}
		return Metadata.fromJson(metadataObject);
	}

	private Response doRequestAndHandleInvalidStatus(URI uri, String bearer, String accept, UUID xCorrelatioId) throws IOException {
		Response resp = doRequest(uri, bearer, accept, xCorrelatioId, UUID.randomUUID());
		if (! (resp.status >= 200 && resp.status < 300)) {
			throw new IOException("Received non-successful status " + resp.status + " for request to " + uri);
		}
		return resp;
	}

	/**
	 * This method performs the actual HTTP get request.
	 * @param uri The URI
	 * @param bearer the bearer token for the Authorization header. This does not include the "Bearer " prefix.
	 * @param accept the value for the accept header.
	 * @param xCorrelationId the correlation id for the X-Correlation-ID header.
	 * @param requestId a unique ID that can be used to uniquely identify the request.
	 * @return the response from the HTTP request. Always return the response, even if the status code indicates a problem.
	 * @throws IOException if a problem occurred with network traffic. Do not throw an exception if the status code indicates a problem!
	 */
	protected abstract Response doRequest(URI uri, String bearer, String accept, UUID xCorrelationId, UUID requestId) throws IOException;
}
