// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TranslationsTestImpl implements Translations {
	@Override
	public String date(LocalDate date) {
		return date.format(DateTimeFormatter.ISO_DATE);
	}

	@Override
	public String timelineFilterProvidersAll() {
		return "timelineFilterProvidersAll";
	}

	@Override
	public String timelineFilterFromDate() {
		return "timelineFilterFromDate";
	}

	@Override
	public String timelineFilterToDate() {
		return "timelineFilterToDate";
	}

	@Override
	public String timelineFilterSortDescending() {
		return "timelineFilterSortDescending";
	}

	@Override
	public String timelineFilterSortAscending() {
		return "timelineFilterSortAscending";
	}

	@Override
	public String timelineFilterSearch() {
		return "timelineFilterSearch";
	}

	@Override
	public String timelineFilterSearchPlaceholder() {
		return "timelineFilterSearchPlaceholder";
	}

	@Override
	public String timelineDisclaimerOverlayContent() {
		return "timelineDisclaimerOverlayContent";
	}

	@Override
	public String timelineDisclaimerOverlayButton() {
		return "timelineDisclaimerOverlayButton";
	}

	@Override
	public String viewerSeriesInfo(int seriesNumber, int seriesSize) {
		return "viewerSeriesInfo";
	}

	@Override
	public String viewerMultiFrameLoader() {
		return "viewerMultiFrameLoader";
	}

	@Override
	public String viewerDownloadButton() {
		return "viewerDownloadButton";
	}

	@Override
	public String viewerDownloadDicomButton() {
		return "viewerDownloadDicomButton";
	}

	@Override
	public String viewerDownloadDicomOverlayContent() {
		return "viewerDownloadDicomOverlayContent";
	}

	@Override
	public String viewerDownloadDicomOverlayPosButton() {
		return "viewerDownloadDicomOverlayPosButton";
	}

	@Override
	public String viewerDownloadDicomOverlayNegButton() {
		return "viewerDownloadDicomOverlayNegButton";
	}

	@Override
	public String viewerNotFoundErrorOverlayContent() {
		return "viewerNotFoundErrorOverlayContent";
	}

	@Override
	public String viewerNotFoundErrorOverlayButton() {
		return "viewerNotFoundErrorOverlayButton";
	}

	@Override
	public String viewerGenericErrorOverlayContent() {
		return "viewerGenericErrorOverlayContent";
	}

	@Override
	public String viewerGenericErrorOverlayButton() {
		return "viewerGenericErrorOverlayButton";
	}

	@Override
	public String viewerTokenErrorOverlayContent() {
		return "viewerTokenErrorOverlayContent";
	}

	@Override
	public String viewerTokenErrorOverlayButton() {
		return "viewerTokenErrorOverlayButton";
	}
}
