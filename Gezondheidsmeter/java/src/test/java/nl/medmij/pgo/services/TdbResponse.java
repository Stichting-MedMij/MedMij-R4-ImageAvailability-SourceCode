// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Test data builder for a {@link WadoRsDownloader.Response} instance.
 */
public class TdbResponse {

	private String mimeType;
	private String inlineBinary;
	private String bulkDataUri;
	private Integer numberOfFrames;

	private TdbResponse() {
	}

	public static TdbResponse aNew() {
		return new TdbResponse();
	}

	public TdbResponse withMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public TdbResponse withInlineBinary(String inlineBinary) {
		this.inlineBinary = inlineBinary;
		return this;
	}

	public TdbResponse withBulkDataUri(String bulkDataUri) {
		this.bulkDataUri = bulkDataUri;
		return this;
	}

	public TdbResponse withNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
		return this;
	}

	/**
	 * Builds a WADO-RS metadata {@link WadoRsDownloader.Response} (i.e. a JSON array containing a single metadata
	 * object), using the given values. Only fields that have been set with one of the methods above are included.
	 */
	public WadoRsDownloader.Response build() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode metadataObject = mapper.createObjectNode();
		if (mimeType != null) {
			metadataObject.putObject(DicomCodes.MIME_TYPE_OF_ENCPASULATED_DOCUMENT).putArray("Value").add(mimeType);
		}
		if (inlineBinary != null || bulkDataUri != null) {
			ObjectNode encapsulatedDocument = metadataObject.putObject(DicomCodes.ENCPASULATED_DOCUMENT);
			if (inlineBinary != null) {
				encapsulatedDocument.put("InlineBinary", inlineBinary);
			}
			if (bulkDataUri != null) {
				encapsulatedDocument.put("BulkDataURI", bulkDataUri);
			}
		}
		if (numberOfFrames != null) {
			metadataObject.putObject(DicomCodes.NUMBER_OF_FRAMES).putArray("Value").add(String.valueOf(numberOfFrames));
		}

		ArrayNode root = mapper.createArrayNode();
		root.add(metadataObject);
		return new WadoRsDownloader.Response("application/dicom+json", 200, new ByteArrayInputStream(mapper.writeValueAsBytes(root)));
	}

}
