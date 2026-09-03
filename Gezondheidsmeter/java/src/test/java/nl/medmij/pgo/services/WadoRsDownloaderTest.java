// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;
import nl.medmij.pgo.domain.models.dicom.Instance;
import nl.medmij.pgo.domain.models.dicom.Kos;
import nl.medmij.pgo.domain.models.dicom.KosFileId;
import nl.medmij.pgo.domain.models.dicom.SopClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class WadoRsDownloaderTest {

	// SOP class UIDs, see SopClass.fromUID
	private static final String BASIC_TEXT_SR_UID = "1.2.840.10008.5.1.4.1.1.88.11";
	private static final String CT_MF_UID = "1.2.840.10008.5.1.4.1.1.2.1";
	private static final String RF_UID = "1.2.840.10008.5.1.4.1.1.12.2";
	private static final String OT_UID = "1.2.840.10008.5.1.4.1.1.104.1";
	private static final String CR_UID = "1.2.840.10008.5.1.4.1.1.1";

	private static final String STUDY_ID = "1.1.1.1";
	private static final String SERIES_ID = "2.1.1.1";
	private static final String RETRIEVE_URL = "https://example.com/studies/" + STUDY_ID + "/series/" + SERIES_ID;

	private static final String BEARER = "bearer-token";
	private static final UUID CORRELATION_ID = UUID.randomUUID();

	/**
	 * A {@link WadoRsDownloader} that records every request it's asked to perform and answers them with
	 * pre-queued responses, so tests don't have to perform any actual network traffic.
	 */
	private static class RecordingDownloader extends WadoRsDownloader {
		final List<RecordedRequest> requests = new ArrayList<>();
		private final Deque<Response> queuedResponses = new ArrayDeque<>();

		void queueResponse(Response response) {
			queuedResponses.add(response);
		}

		@Override
		protected Response doRequest(URI uri, String bearer, String accept, UUID xCorrelationId, UUID requestId) throws IOException {
			requests.add(new RecordedRequest(uri, bearer, accept, xCorrelationId));
			Response response = queuedResponses.poll();
			if (response == null) {
				throw new IllegalStateException("Test setup error: no response queued for request to " + uri);
			}
			return response;
		}
	}

	private static class RecordedRequest {
		final URI uri;
		final String bearer;
		final String accept;
		final UUID xCorrelationId;

		RecordedRequest(URI uri, String bearer, String accept, UUID xCorrelationId) {
			this.uri = uri;
			this.bearer = bearer;
			this.accept = accept;
			this.xCorrelationId = xCorrelationId;
		}
	}

	@Test
	public void doDownload_structuredReport_throwsSopClassNotSupportedException() throws IOException {
		Instance instance = buildInstance(BASIC_TEXT_SR_UID, "9.9.9.1");
		RecordingDownloader downloader = new RecordingDownloader();

		try {
			downloader.doDownload(BEARER, CORRELATION_ID, instance);
			fail("Expected SopClassNotSupportedException");
		} catch (WadoRsDownloader.SopClassNotSupportedException e) {
			assertEquals(SopClass.BASIC_TEXT_SR, e.sopClass);
		}
		assertTrue("No request should have been made", downloader.requests.isEmpty());
	}

	@Test
	public void doDownload_multiFrameImageOrVideo_returnsFrameCountAsJson() throws Exception {
		assertDownloadOfMultiFrameSopClassReturnsFrameCount(CT_MF_UID);
	}

	@Test
	public void doDownload_video_returnsFrameCountAsJson() throws Exception {
		assertDownloadOfMultiFrameSopClassReturnsFrameCount(RF_UID);
	}

	private void assertDownloadOfMultiFrameSopClassReturnsFrameCount(String sopClassUid) throws Exception {
		Instance instance = buildInstance(sopClassUid, "9.9.9.2");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(TdbResponse.aNew().withNumberOfFrames(12).build());

		WadoRsDownloader.Response response = downloader.doDownload(BEARER, CORRELATION_ID, instance);

		assertEquals("application/json", response.contentType);
		assertEquals("{\"numberOfFrames\": 12}", readAll(response.contents));
		assertEquals(1, downloader.requests.size());
		RecordedRequest request = downloader.requests.get(0);
		assertEquals("application/dicom+json", request.accept);
		assertEquals(BEARER, request.bearer);
		assertEquals(CORRELATION_ID, request.xCorrelationId);
		assertTrue(request.uri.toString().endsWith("/metadata"));
	}

	@Test
	public void doDownload_pdfWithInlineBinary_returnsDecodedDocument() throws Exception {
		Instance instance = buildInstance(OT_UID, "9.9.9.3");
		RecordingDownloader downloader = new RecordingDownloader();
		String encodedDocument = Base64.getEncoder().encodeToString("hello pdf".getBytes(StandardCharsets.UTF_8));
		downloader.queueResponse(TdbResponse.aNew()
				.withMimeType("application/pdf")
				.withInlineBinary(encodedDocument)
				.build());

		WadoRsDownloader.Response response = downloader.doDownload(BEARER, CORRELATION_ID, instance);

		assertEquals("application/pdf", response.contentType);
		assertEquals("hello pdf", readAll(response.contents));
		assertEquals("Only the metadata request should have been made", 1, downloader.requests.size());
	}

	@Test
	public void doDownload_pdfWithBulkDataUri_downloadsRenderedInstanceInstead() throws Exception {
		Instance instance = buildInstance(OT_UID, "9.9.9.4");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(TdbResponse.aNew()
				.withMimeType("application/pdf")
				.withBulkDataUri("https://example.com/bulkdata/1")
				.build());
		downloader.queueResponse(plainResponse("application/pdf", 200, "rendered-bytes"));

		WadoRsDownloader.Response response = downloader.doDownload(BEARER, CORRELATION_ID, instance);

		// Next to the download of the metadata, the rendered instance has also been downloaded
		assertEquals(2, downloader.requests.size());

		// Verify request for the rendered document: the BulkDataURI is not followed directly.
		// Instead, the instance-level "/rendered" sub-resource is requested, using the mime type from the metadata as the accept header.
		RecordedRequest renderedRequest = downloader.requests.get(1);
		assertEquals(
				"https://example.com/studies/" + STUDY_ID + "/series/" + SERIES_ID + "/instances/9.9.9.4/rendered",
				renderedRequest.uri.toString());
		assertEquals("application/pdf", renderedRequest.accept);

		// Verify the response returns the rendered document
		assertEquals("application/pdf", response.contentType);
		assertEquals("rendered-bytes", readAll(response.contents));
	}

	@Test
	public void doDownload_pdfWithBulkDataUriAndNoMimeType_defaultsAcceptHeaderToOctetStream() throws Exception {
		Instance instance = buildInstance(OT_UID, "9.9.9.5");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(TdbResponse.aNew().withBulkDataUri("https://example.com/bulkdata/2").build());
		downloader.queueResponse(plainResponse("application/pdf", 200, "rendered-bytes"));

		downloader.doDownload(BEARER, CORRELATION_ID, instance);

		assertEquals(2, downloader.requests.size());
		assertEquals("application/octet-stream", downloader.requests.get(1).accept);
	}

	@Test
	public void doDownload_pdfWithoutInlineBinaryOrBulkDataUri_returnsNoContent() throws Exception {
		Instance instance = buildInstance(OT_UID, "9.9.9.6");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(TdbResponse.aNew().withMimeType("application/pdf").build());

		WadoRsDownloader.Response response = downloader.doDownload(BEARER, CORRELATION_ID, instance);

		assertEquals("application/pdf", response.contentType);
		assertEquals(204, response.status);
		assertEquals("", readAll(response.contents));
		assertEquals("Only the metadata request should have been made", 1, downloader.requests.size());
	}

	@Test
	public void doDownload_supportedImage_requestsRenderedJpeg() throws Exception {
		Instance instance = buildInstance(CR_UID, "9.9.9.7");
		RecordingDownloader downloader = new RecordingDownloader();
		WadoRsDownloader.Response queued = plainResponse("image/jpeg", 200, "jpeg-bytes");
		downloader.queueResponse(queued);

		WadoRsDownloader.Response response = downloader.doDownload(BEARER, CORRELATION_ID, instance);

		assertSame(queued, response);
		assertEquals(1, downloader.requests.size());
		RecordedRequest request = downloader.requests.get(0);
		assertEquals("image/jpeg", request.accept);
		assertTrue(request.uri.toString().endsWith("/rendered"));
		assertFalse(request.uri.toString().contains("/metadata"));
	}

	@Test
	public void doDownloadFrame_multiFrameImage_buildsFrameUrl() throws Exception {
		Instance instance = buildInstance(CT_MF_UID, "9.9.9.8");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(plainResponse("image/jpeg", 200, "frame-bytes"));

		downloader.doDownloadFrame(BEARER, CORRELATION_ID, instance, 3);

		assertEquals(1, downloader.requests.size());
		RecordedRequest request = downloader.requests.get(0);
		assertEquals("image/jpeg", request.accept);
		assertTrue(request.uri.toString().endsWith("/instances/9.9.9.8/frames/3/rendered"));
	}

	@Test
	public void doDownloadFrame_unsupportedSopClass_throwsSopClassNotSupportedException() {
		Instance instance = buildInstance(CR_UID, "9.9.9.9");
		RecordingDownloader downloader = new RecordingDownloader();

		try {
			downloader.doDownloadFrame(BEARER, CORRELATION_ID, instance, 1);
			fail("Expected SopClassNotSupportedException");
		} catch (Exception e) {
			assertTrue(e instanceof WadoRsDownloader.SopClassNotSupportedException);
			assertEquals(SopClass.CR, ((WadoRsDownloader.SopClassNotSupportedException) e).sopClass);
		}
		assertTrue(downloader.requests.isEmpty());
	}

	@Test
	public void doDownloadDicom_requestsRawDicomWithoutSubEndpoint() throws Exception {
		Instance instance = buildInstance(CR_UID, "9.9.9.10");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(plainResponse("application/dicom", 200, "dicom-bytes"));

		downloader.doDownloadDicom(BEARER, CORRELATION_ID, instance);

		assertEquals(1, downloader.requests.size());
		RecordedRequest request = downloader.requests.get(0);
		assertEquals("application/dicom", request.accept);
		assertTrue(request.uri.toString().endsWith("/instances/9.9.9.10"));
	}

	@Test
	public void doDownload_metadataRequestReturnsErrorStatus_throwsIOException() {
		Instance instance = buildInstance(OT_UID, "9.9.9.11");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(plainResponse("text/plain", 500, "server error"));

		try {
			downloader.doDownload(BEARER, CORRELATION_ID, instance);
			fail("Expected IOException");
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("500"));
		} catch (WadoRsDownloader.SopClassNotSupportedException e) {
			fail("Did not expect SopClassNotSupportedException");
		}
	}

	@Test
	public void doDownload_metadataResponseMissingObject_throwsIOException() {
		Instance instance = buildInstance(OT_UID, "9.9.9.12");
		RecordingDownloader downloader = new RecordingDownloader();
		downloader.queueResponse(plainResponse("application/dicom+json", 200, "[]"));

		try {
			downloader.doDownload(BEARER, CORRELATION_ID, instance);
			fail("Expected IOException");
		} catch (IOException e) {
			assertEquals("Did not receive proper metadata response", e.getMessage());
		} catch (WadoRsDownloader.SopClassNotSupportedException e) {
			fail("Did not expect SopClassNotSupportedException");
		}
	}

	/**
	 * Builds an {@link Instance} belonging to a study/series with a valid WADO-RS retrieve url, for the given
	 * SOP class, by round-tripping through {@link Kos#fromJson}
	 */
	private Instance buildInstance(String sopClassUid, String instanceId) {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode instanceNode = mapper.createObjectNode();
		instanceNode.putObject(DicomCodes.REFERENCED_SOP_CLASS_UID).putArray("Value").add(sopClassUid);
		instanceNode.putObject(DicomCodes.REFERENCED_SOP_INSTANCE_UID).putArray("Value").add(instanceId);

		ObjectNode seriesNode = mapper.createObjectNode();
		seriesNode.putObject(DicomCodes.SERIES_INSTANCE_UID).putArray("Value").add(SERIES_ID);
		seriesNode.putObject(DicomCodes.RETRIEVE_URL).putArray("Value").add(RETRIEVE_URL);
		seriesNode.putObject(DicomCodes.REFERENCED_SOP_SEQUENCE).putArray("Value").add(instanceNode);

		ObjectNode currentRequestedProcedureEvidence = mapper.createObjectNode();
		currentRequestedProcedureEvidence.putObject(DicomCodes.REFERENCED_SERIES_SEQUENCE).putArray("Value").add(seriesNode);

		ObjectNode studyNode = mapper.createObjectNode();
		studyNode.putObject(DicomCodes.STUDY_INSTANCE_UID).putArray("Value").add(STUDY_ID);
		studyNode.putObject(DicomCodes.STUDY_DATE).putArray("Value").add("20250101");
		studyNode.putObject(DicomCodes.CURRENT_REQUESTED_PROCEDURE_EVIDENCE_SEQUENCE).putArray("Value").add(currentRequestedProcedureEvidence);

		ArrayNode root = mapper.createArrayNode();
		root.add(studyNode);

		Kos kos = Kos.fromJson(root, KosFileId.ofString("test"));
		return kos.getInstances().get(0);
	}

	private WadoRsDownloader.Response plainResponse(String contentType, int status, String body) {
		return new WadoRsDownloader.Response(contentType, status, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
	}

	private String readAll(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}
}
