// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MetadataTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void fromJson_withInlineBinaryAndBulkDataUri_parsesAllFields() throws IOException {
		JsonNode dicomJson = MAPPER.readTree(getClass().getResourceAsStream("metadata_with_inline_binary_and_bulk_data_uri.json"));

		Metadata metadata = Metadata.fromJson(dicomJson);

		assertEquals("ISO_IR 192", metadata.getEncoding());
		assertEquals("application/pdf", metadata.getMimeType());
		assertTrue(metadata.hasData());
		assertEquals("dGVzdC1kYXRh", metadata.getData());
		assertEquals(3, metadata.getNumberOfFrames());
		assertTrue(metadata.hasBulkDataUri());
		assertEquals("https://example.com/studies/1/series/2/instances/3/bulkdata/7FE00010", metadata.getBulkDataUri());
	}

	@Test
	public void fromJson_missingFields_fallsBackToDefaults() throws Exception {
		JsonNode dicomJson = MAPPER.readTree("{}");

		Metadata metadata = Metadata.fromJson(dicomJson);

		assertEquals("ISO_IR 100", metadata.getEncoding());
		assertEquals("", metadata.getMimeType());
		assertFalse(metadata.hasData());
		assertEquals("", metadata.getData());
		assertFalse(metadata.hasBulkDataUri());
		assertEquals("", metadata.getBulkDataUri());
		assertEquals(0, metadata.getNumberOfFrames());
	}
}
