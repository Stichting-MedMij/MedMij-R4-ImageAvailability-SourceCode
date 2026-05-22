// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.medmij.pgo.domain.models.dicom.Kos;
import nl.medmij.pgo.domain.models.dicom.KosFileId;
import org.junit.BeforeClass;

import java.io.InputStream;

public abstract class DicomTest {
	protected static Kos sampleKos;

	@BeforeClass
	public static void readJson() throws Exception {
		InputStream inputStream = DicomTest.class.getResourceAsStream("sample_kos.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(inputStream);
		sampleKos = Kos.fromJson(node, KosFileId.ofString("sample"));
	}
}
