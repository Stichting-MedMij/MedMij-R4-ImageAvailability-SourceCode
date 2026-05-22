// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import nl.medmij.pgo.DicomTest;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class KosTest extends DicomTest {

	@Test
	public void testToJson() {
		assertEquals("1.1.1.1", sampleKos.getStudies().get(0).getStudyInstanceId());
		assertEquals(LocalDate.of(2025, 9, 1), sampleKos.getStudies().get(0).getStudyDate());
		assertEquals("Test", sampleKos.getStudies().get(0).getHcpName());
		assertEquals(2, sampleKos.getSeries().size());
		assertEquals("2.1.1.1", sampleKos.getStudies().get(0).getSeries().get(0).getSeriesInstanceId());
		assertEquals("2.1.2.1", sampleKos.getStudies().get(0).getSeries().get(1).getSeriesInstanceId());
		assertEquals(sampleKos.getStudies().get(0).getSeries().get(0), sampleKos.getSeries().get(0));
		assertEquals(sampleKos.getStudies().get(0).getSeries().get(1), sampleKos.getSeries().get(1));
		assertEquals(4, sampleKos.getInstances().size());
		assertEquals(2, sampleKos.getSeries().get(0).getInstances().size());
		assertEquals(sampleKos.getInstances().get(2), sampleKos.getSeries().get(1).getInstances().get(0));
		assertTrue(sampleKos.getInstances().get(2).isSupported());
		assertFalse(sampleKos.getInstances().get(1).isSupported());
		assertEquals("www.example.com/studies/1.1.1.1/series/2.1.1.1", sampleKos.getSeries().get(0).getRetrieveUrl());
	}

}