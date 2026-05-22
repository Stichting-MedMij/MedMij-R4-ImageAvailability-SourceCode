// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates.viewer;

import nl.medmij.pgo.DicomTest;
import nl.medmij.pgo.templates.TranslationsTestImpl;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ViewerTemplateTest extends DicomTest {

	@Test
	public void testViewerGeneration() throws Exception {
		InputStream inputStream = ViewerTemplateTest.class.getResourceAsStream("sample_viewer.html");
		StringBuilder sampleHtml = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			int c = 0;
			while ((c = reader.read()) != -1) {
				sampleHtml.append((char) c);
			}
		}

		assertEquals(sampleHtml.toString().replaceAll("\\r?\\n", "").replaceAll("\\t", ""), ViewerTemplate.generateViewerHtml(new TranslationsTestImpl(), sampleKos, "testUrl").replaceAll("\\r?\\n", "").replaceAll("\\t", ""));
	}
}