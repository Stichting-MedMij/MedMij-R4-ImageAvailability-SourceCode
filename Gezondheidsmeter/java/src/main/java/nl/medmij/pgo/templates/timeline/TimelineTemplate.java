// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates.timeline;

import nl.medmij.pgo.domain.models.timeline.Timeline;
import nl.medmij.pgo.templates.Translations;

import java.time.Month;
import java.time.format.DateTimeFormatter;

public class TimelineTemplate {
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	private TimelineTemplate() {
		// Remove implicit public constructor
	}

	private static String format(Month month) {
		int monthValue = month.getValue();
		return monthValue < 10
				? "0" + monthValue
				: "" + monthValue;
	}

	public static String generateTimelineHtml(Translations translations, Timeline timeline) {
		return "<div class=\"medmij-timeline\" id=\"medmij-timeline\">\n"
				+ generateDisclaimer(translations)
				+ generateTimelineFilterHtml(translations, timeline)
				+ generateTimelineDataHtml(timeline)
				+ "</div>\n";
	}

	private static String generateDisclaimer(Translations translations) {
		StringBuilder html = new StringBuilder();
		html.append("\t<div id=\"disclaimer\" class=\"overlay beeld-confirm\">\n");
		html.append("\t\t<div class=\"overlay-content\">\n");
		html.append("\t\t\t<div class=\"overlay-inner\">\n");
		html.append("\t\t\t\t<div class=\"group\">").append(translations.timelineDisclaimerOverlayContent()).append("</div>\n");
		html.append("\t\t\t\t<button type=\"button\" class=\"primary\" onclick=\"document.getElementById('disclaimer').classList.toggle('hidden');\">").append(translations.timelineDisclaimerOverlayButton()).append("</button>\n");
		html.append("\t\t\t</div>\n");
		html.append("\t\t</div>\n");
		html.append("\t</div>\n");
		return html.toString();
	}

	private static String generateTimelineFilterHtml(Translations translations, Timeline timeline) {
		StringBuilder html = new StringBuilder();
		html.append("\t<div class=\"timeline-filter\">\n");
		html.append("\t\t<div class=\"hcp\">\n");
		html.append("\t\t\t<select name=\"provider\" id=\"medmij-timeline-provider\">\n");
		html.append("\t\t\t\t<option value=\"all\">").append(translations.timelineFilterProvidersAll()).append("</option>\n");
		timeline.getHealthcareProviders().forEach(s -> html.append("\t\t\t\t<option value=\"").append(s).append("\">").append(s).append("</option>\n"));
		html.append("\t\t\t</select>\n");
		html.append("\t\t</div>\n");
		html.append("\t\t<div class=\"from\">\n");
		html.append("\t\t\t<label for=\"medmij-timeline-from\">").append(translations.timelineFilterFromDate()).append("</label>\n");
		html.append("\t\t\t<input type=\"date\" name=\"from\" id=\"medmij-timeline-from\"/>\n");
		html.append("\t\t</div>\n");
		html.append("\t\t<div class=\"to\">\n");
		html.append("\t\t\t<label for=\"medmij-timeline-to\">").append(translations.timelineFilterToDate()).append("</label>\n");
		html.append("\t\t\t<input type=\"date\" name=\"to\" id=\"medmij-timeline-to\"/>\n");
		html.append("\t\t</div>\n");
		html.append("\t\t<div class=\"order\">\n");
		html.append("\t\t\t<select name=\"order\" id=\"medmij-timeline-order\">\n");
		html.append("\t\t\t\t<option value=\"order-desc\">").append(translations.timelineFilterSortDescending()).append("</option>\n");
		html.append("\t\t\t\t<option value=\"order-asc\">").append(translations.timelineFilterSortAscending()).append("</option>\n");
		html.append("\t\t\t</select>\n");
		html.append("\t\t</div>\n");
		html.append("\t\t<div class=\"search\">\n");
		html.append("\t\t\t<label for=\"medmij-timeline-search\">").append(translations.timelineFilterSearch()).append("</label>\n");
		html.append("\t\t\t<input type=\"text\" name=\"searchTitle\" id=\"medmij-timeline-search\" placeholder=\"").append(translations.timelineFilterSearchPlaceholder()).append("\"/>\n");
		html.append("\t\t</div>\n");
		html.append("\t</div>\n");
		return html.toString();
	}

	private static String generateTimelineDataHtml(Timeline timeline) {
		StringBuilder html = new StringBuilder();
		html.append("\t<div class=\"timeline-data\">\n");
		timeline.getYears().forEach(year -> {
			html.append("\t\t<div class=\"timeline-year\" data-timeline-year-value=\"").append(year.toString()).append("\">\n");
			timeline.getMonths(year).forEach(month -> {
				html.append("\t\t\t<div class=\"timeline-month\" data-timeline-month-value=\"").append(format(month)).append("\">\n");
				timeline.getDays(year, month).forEach(day -> {
					html.append("\t\t\t\t<div class=\"timeline-day\" data-timeline-day-value=\"").append(day.format(DAY_FORMATTER)).append("\">\n");
					html.append("\t\t\t\t\t<div class=\"timeline-marker\" data-timeline-marker-date=\"").append(day.format(DATE_FORMATTER)).append("\"></div>\n");
					timeline.getEntries(day).forEach(entry -> html.append("\t\t\t\t\t<div class=\"timeline-entry\" data-timeline-hcp=\"").append(entry.getHealthcareProvider()).append("\">").append(entry.getContent()).append("</div>\n"));
					html.append("\t\t\t\t</div>\n");
				});
				html.append("\t\t\t</div>\n");
			});
			html.append("\t\t</div>\n");
		});
		timeline.getUnsortedEntries().forEach(entry -> html.append("\t\t<div class=\"timeline-entry\">").append(entry.getContent()).append("</div>\n"));
		html.append("\t</div>\n");
		return html.toString();
	}
}
