// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.timeline;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class Timeline {

	private final Map<Year, Map<Month, Map<LocalDate, List<TimelineEntry>>>> entries;
	private final List<TimelineEntry> unsortedEntries;
	private final Set<String> healthcareProviders;

	/**
	 * Create a timeline from the supplied entries. These entries don't need to be ordered
	 */
	public Timeline(Collection<TimelineEntry> entries) {
		this.entries = new HashMap<>();
		entries.stream()
				.filter(entry -> entry.getClock() != null)
				// the minus sign will cause this to order in reverse
				.sorted(Comparator.comparing(entry -> -entry.getClock().millis()))
				.forEachOrdered(entry -> {
					LocalDate date = LocalDate.now(entry.getClock());
					this.entries
							.computeIfAbsent(Year.from(date), k -> new EnumMap<>(Month.class))
							.computeIfAbsent(Month.from(date), k -> new HashMap<>())
							.computeIfAbsent(date, k -> new ArrayList<>())
							.add(entry);
				});
		unsortedEntries = entries.stream()
				.filter(entry -> entry.getClock() == null)
				.collect(Collectors.toList());
		this.healthcareProviders = entries.stream()
				.map(TimelineEntry::getHealthcareProvider)
				.collect(Collectors.toSet());
	}

	public List<Year> getYears() {
		return entries.keySet()
				.stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
	}

	/**
	 * Get all months for the supplied year in descending order of {@link TimelineEntry#getClock()}
	 */
	public List<Month> getMonths(Year year) {
		return entries
				.getOrDefault(year, Collections.emptyMap())
				.keySet()
				.stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
	}

	/**
	 * Get all dates for the supplied year and month in descending order of {@link TimelineEntry#getClock()}
	 */
	public List<LocalDate> getDays(Year year, Month month) {
		return entries
				.getOrDefault(year, Collections.emptyMap())
				.getOrDefault(month, Collections.emptyMap())
				.keySet()
				.stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
	}

	/**
	 * Get all entries for the supplied date in descending order of {@link TimelineEntry#getClock()}
	 */
	public List<TimelineEntry> getEntries(LocalDate date) {
		List<TimelineEntry> entriesForDate = entries
				.getOrDefault(Year.from(date), Collections.emptyMap())
				.getOrDefault(Month.from(date), Collections.emptyMap())
				.getOrDefault(date, Collections.emptyList());
		// Return a new list, so our internal list can't be polluted by actions in the call site
		return new ArrayList<>(entriesForDate);
	}

	/**
	 * Get all entries for which {@link TimelineEntry#getClock()} returns null
	 */
	public List<TimelineEntry> getUnsortedEntries() {
		// Return a new list, so our internal list can't be polluted by actions in the call site
		return new ArrayList<>(unsortedEntries);
	}

	public Set<String> getHealthcareProviders() {
		// Return a new set, so our internal set can't be polluted by actions in the call site
		return new HashSet<>(healthcareProviders);
	}
}
