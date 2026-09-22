// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;
import nl.medmij.pgo.util.JsonUtil;

public class Metadata {

	private final String encoding;
	private final String mimeType;
	private final String data;
	private final String bulkDataUri;
	private final int numberOfFrames;

	private Metadata(String encoding, String mimeType, String data, String bulkDataUri, int numberOfFrames) {
		this.encoding = encoding;
		this.mimeType = mimeType;
		this.data = data == null ? "" : data;
		this.bulkDataUri = bulkDataUri == null ? "" : bulkDataUri;
		this.numberOfFrames = numberOfFrames;
	}

	/**
	 * Parse the provided {@link JsonNode} as Metadata object
	 */
	public static Metadata fromJson(JsonNode jsonNode) {
		String encoding = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, jsonNode.get(DicomCodes.SPECIFIC_CHARACTER_SET), "ISO_IR 100");
		String mimeType = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, jsonNode.get(DicomCodes.MIME_TYPE_OF_ENCPASULATED_DOCUMENT), "");
		JsonNode encapsulatedDocument = jsonNode.path(DicomCodes.ENCPASULATED_DOCUMENT);
		String data = encapsulatedDocument.path("InlineBinary").textValue();
		String bulkDataUri = encapsulatedDocument.path("BulkDataURI").textValue();
		int numberOfFrames = Integer.parseInt(JsonUtil.getFirstValueInterpreted(JsonNode::textValue, jsonNode.get(DicomCodes.NUMBER_OF_FRAMES), "0"));
		return new Metadata(encoding, mimeType, data, bulkDataUri, numberOfFrames);
	}

	public String getEncoding() {
		return encoding;
	}

	public String getMimeType() {
		return mimeType;
	}

	public boolean hasData() {
		return ! data.isEmpty();
	}

	public String getData() {
		return data;
	}

	public boolean hasBulkDataUri() {
		return ! bulkDataUri.isEmpty();
	}

	public String getBulkDataUri() {
		return bulkDataUri;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}
}
