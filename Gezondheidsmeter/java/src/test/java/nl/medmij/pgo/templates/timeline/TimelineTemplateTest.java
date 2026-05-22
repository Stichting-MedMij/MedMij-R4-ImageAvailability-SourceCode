// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates.timeline;

import nl.medmij.pgo.domain.models.timeline.Timeline;
import nl.medmij.pgo.domain.models.timeline.TimelineEntry;
import nl.medmij.pgo.domain.models.timeline.TimelineTestEntry;
import nl.medmij.pgo.templates.TranslationsTestImpl;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimelineTemplateTest {

	@Test
	public void testTimelineGeneration() throws Exception {
		InputStream inputStream = TimelineTemplateTest.class.getResourceAsStream("sample_timeline.html");
		StringBuilder sampleHtml = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			int c = 0;
			while ((c = reader.read()) != -1) {
				sampleHtml.append((char) c);
			}
		}

		List<TimelineEntry> testEntries = new ArrayList<>();
		testEntries.add(new TimelineTestEntry(null, "a", ""));
		testEntries.add(new TimelineTestEntry(null, "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2021-01-01T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2021-01-11T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2021-01-02T12:00:00Z"), ZoneId.of("UTC")), "b", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2021-02-01T12:00:00Z"), ZoneId.of("UTC")), "b", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2024-11-01T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2024-12-01T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2024-11-01T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2024-02-01T12:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-01T12:00:00Z"), ZoneId.of("UTC")), "a", "1"));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-01T12:00:00Z"), ZoneId.of("UTC")), "a", "1"));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-01T13:00:00Z"), ZoneId.of("UTC")), "a", "2"));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-01T11:00:00Z"), ZoneId.of("UTC")), "a", "3"));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-02T14:00:00Z"), ZoneId.of("UTC")), "a", ""));
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-07-01T13:00:00Z"), ZoneId.of("UTC")), "a", ""));

		Timeline timeline = new Timeline(testEntries);

		assertEquals(sampleHtml.toString().replaceAll("\\r?\\n", "").replaceAll("\\t", ""), TimelineTemplate.generateTimelineHtml(new TranslationsTestImpl(), timeline).replaceAll("\\r?\\n", "").replaceAll("\\t", ""));
	}
}