// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.services;

import nl.medmij.pgo.DicomTest;
import nl.medmij.pgo.domain.models.dicom.Instance;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class WadoRsBuilderTest extends DicomTest {

	@Test
	public void testInstantiation() {
		Instance instanceWithRetrievalUrl = sampleKos
				.getStudies().get(0)
				.getSeries().get(0)
				.getInstances().get(0);
		Instance instanceWithoutRetrievalUrl = sampleKos
				.getStudies().get(0)
				.getSeries().get(1)
				.getInstances().get(0);

		WadoRsBuilder builder = WadoRsBuilder.fromSeries(instanceWithRetrievalUrl.getSeries());
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1.1.1.1", builder.getStudyInstanceId());
		assertEquals("2.1.1.1", builder.getSeriesInstanceId());
		assertEquals("", builder.getInstanceId());

		try {
			WadoRsBuilder.fromSeries(instanceWithoutRetrievalUrl.getSeries());
			fail();
		} catch (IllegalArgumentException e) {
			// this is expected
		}

		builder = WadoRsBuilder.fromInstance(instanceWithRetrievalUrl);
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1.1.1.1", builder.getStudyInstanceId());
		assertEquals("2.1.1.1", builder.getSeriesInstanceId());
		assertEquals("3.1.1.1", builder.getInstanceId());



		try {
			WadoRsBuilder.fromInstance(instanceWithoutRetrievalUrl);
			fail();
		} catch (IllegalArgumentException e) {
			// this is expected
		}
	}

	@Test
	public void testCopy() {
		WadoRsBuilder original = new WadoRsBuilder(URI.create("www.example.com"))
				.studyInstanceId("1")
				.seriesInstanceId("2")
				.instanceId("3");
		WadoRsBuilder copy = original.copy();
		assertEquals(original, copy);
		copy.studyInstanceId("4");
		assertNotEquals(original, copy);
	}

	@Test
	public void testUpdate() {
		Instance instance = sampleKos
				.getStudies().get(0)
				.getSeries().get(0)
				.getInstances().get(0);

		WadoRsBuilder builder = new WadoRsBuilder(URI.create("www.example.com"))
				.studyInstanceId("1")
				.seriesInstanceId("2")
				.instanceId("3");
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1", builder.getStudyInstanceId());
		assertEquals("2", builder.getSeriesInstanceId());
		assertEquals("3", builder.getInstanceId());

		builder.study(instance.getSeries().getStudy());
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1.1.1.1", builder.getStudyInstanceId());
		assertEquals("2", builder.getSeriesInstanceId());
		assertEquals("3", builder.getInstanceId());

		builder = new WadoRsBuilder(URI.create("www.example.com"))
				.studyInstanceId("1")
				.seriesInstanceId("2")
				.instanceId("3");
		builder.series(instance.getSeries());
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1.1.1.1", builder.getStudyInstanceId());
		assertEquals("2.1.1.1", builder.getSeriesInstanceId());
		assertEquals("3", builder.getInstanceId());

		builder = new WadoRsBuilder(URI.create("www.example.com"))
				.studyInstanceId("1")
				.seriesInstanceId("2")
				.instanceId("3");
		builder.instance(sampleKos.getStudies().get(0).getSeries().get(0).getInstances().get(0));
		assertEquals("www.example.com", builder.getBase().toString());
		assertEquals("1.1.1.1", builder.getStudyInstanceId());
		assertEquals("2.1.1.1", builder.getSeriesInstanceId());
		assertEquals("3.1.1.1", builder.getInstanceId());
	}

	@Test
	public void testGenerateUrls() {
		WadoRsBuilder builder = new WadoRsBuilder(URI.create("www.example.com"));

		try {
			builder.generateStudyUrl();
			// Should throw error, because study is not defined
			fail();
		} catch (IllegalArgumentException e) {
			// This is expected
		}

		builder.studyInstanceId("1");
		assertEquals("www.example.com/studies/1", builder.generateStudyUrl().toString());

		try {
			builder.generateSeriesUrl();
			// Should throw error, because series is not defined
			fail();
		} catch (IllegalArgumentException e) {
			// This is expected
		}

		builder.seriesInstanceId("2");
		assertEquals("www.example.com/studies/1", builder.generateStudyUrl().toString());
		assertEquals("www.example.com/studies/1/series/2", builder.generateSeriesUrl().toString());

		try {
			builder.generateInstancesUrl();
			// Should throw error, because instance is not defined
			fail();
		} catch (IllegalArgumentException e) {
			// This is expected
		}

		builder.instanceId("3");
		assertEquals("www.example.com/studies/1", builder.generateStudyUrl().toString());
		assertEquals("www.example.com/studies/1/series/2", builder.generateSeriesUrl().toString());
		assertEquals("www.example.com/studies/1/series/2/instances/3", builder.generateInstancesUrl().toString());

		builder.endpoint(WadoRsBuilder.Endpoint.RENDERED);
		assertEquals("www.example.com/studies/1/rendered", builder.generateStudyUrl().toString());
		assertEquals("www.example.com/studies/1/series/2/rendered", builder.generateSeriesUrl().toString());
		assertEquals("www.example.com/studies/1/series/2/instances/3/rendered", builder.generateInstancesUrl().toString());
	}
}