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
		return new Response(metadata.getMimeType(), resp.status, new ByteArrayInputStream(Base64.getDecoder().decode(metadata.getData())));
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
		return doRequest(url, bearer, accept, xCorrelatioId, UUID.randomUUID());
	}

	private Metadata mapResponseToMetadata(Response resp) throws IOException {
		if (! (resp.status >= 200 && resp.status < 300)) {
			throw new IOException("Received error response");
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode metadataArray = mapper.readTree(resp.contents);
		JsonNode metadataObject = metadataArray.get(0);
		if (metadataObject == null) {
			throw new IOException("Did not receive proper metadata response");
		}
		return Metadata.fromJson(metadataObject);
	}

	protected abstract Response doRequest(URI uri, String bearer, String accept, UUID xCorrelationId, UUID medmijRequestId) throws IOException;
}
