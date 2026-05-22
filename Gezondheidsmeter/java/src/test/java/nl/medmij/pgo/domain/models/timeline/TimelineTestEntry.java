// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.timeline;

import java.time.Clock;

public class TimelineTestEntry implements TimelineEntry {
	private final Clock clock;
	private final String category;
	private final String content;

	public TimelineTestEntry(Clock clock, String category, String content) {
		this.clock = clock;
		this.category = category;
		this.content = content;
	}

	@Override
	public Clock getClock() {
		return clock;
	}

	@Override
	public String getHealthcareProvider() {
		return category;
	}

	@Override
	public String getContent() {
		return content;
	}
}
