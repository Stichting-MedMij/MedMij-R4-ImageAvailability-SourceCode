// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.timeline;

import org.junit.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimelineTest {

	@Test
	public void testTimeline() {
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

		// Test order and contents of getYears()
		List<Year> years = timeline.getYears();
		assertEquals(3, years.size());
		assertEquals(Year.of(2024), years.get(0));
		assertEquals(Year.of(2021), years.get(1));
		assertEquals(Year.of(2020), years.get(2));

		// Test order and contents of getMonths()
		List<Month> months = timeline.getMonths(Year.of(2024));
		assertEquals(3, months.size());
		assertEquals(Month.DECEMBER, months.get(0));
		assertEquals(Month.NOVEMBER, months.get(1));
		assertEquals(Month.FEBRUARY, months.get(2));

		// Test order and contents of getDays()
		List<LocalDate> days = timeline.getDays(Year.of(2021), Month.JANUARY);
		assertEquals(3, days.size());
		assertEquals(LocalDate.of(2021, 1, 11), days.get(0));
		assertEquals(LocalDate.of(2021, 1, 2), days.get(1));
		assertEquals(LocalDate.of(2021, 1, 1), days.get(2));

		//Test order and contents of getEntries()
		List<TimelineEntry> entries = timeline.getEntries(LocalDate.of(2020, 6, 1));
		assertEquals(4, entries.size());
		assertEquals("2", entries.get(0).getContent());
		assertEquals("1", entries.get(1).getContent());
		assertEquals("1", entries.get(2).getContent());
		assertEquals("3", entries.get(3).getContent());

		// Test contents of getUnsortedEntries()
		List<TimelineEntry> unsortedEntries = timeline.getUnsortedEntries();
		assertEquals(2, unsortedEntries.size());

		// Test categories
		assertEquals(2, timeline.getHealthcareProviders().size());

		// Test immutability of timeline
		testEntries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2022-01-01T12:00:00Z"), ZoneId.of("UTC")), "c", ""));
		assertEquals(3, timeline.getYears().size());
		years.add(Year.of(2022));
		assertEquals(3, timeline.getYears().size());
		months.add(Month.APRIL);
		assertEquals(3, timeline.getMonths(Year.of(2024)).size());
		days.add(LocalDate.of(2021, 1, 5));
		assertEquals(3, timeline.getDays(Year.of(2021), Month.JANUARY).size());
		entries.add(new TimelineTestEntry(Clock.fixed(Instant.parse("2020-06-01T14:00:00Z"), ZoneId.of("UTC")), "c", ""));
		assertEquals(4, timeline.getEntries(LocalDate.of(2020, 6, 1)).size());
		unsortedEntries.add(new TimelineTestEntry(null, "c", ""));
		assertEquals(2, timeline.getUnsortedEntries().size());
		assertEquals(2, timeline.getHealthcareProviders().size());
	}
}