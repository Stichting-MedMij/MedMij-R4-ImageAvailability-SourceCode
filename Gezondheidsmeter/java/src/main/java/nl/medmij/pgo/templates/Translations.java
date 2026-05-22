// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates;

import java.time.LocalDate;

public interface Translations {
	String date(LocalDate date);

	String timelineFilterProvidersAll();
	String timelineFilterFromDate();
	String timelineFilterToDate();
	String timelineFilterSortDescending();
	String timelineFilterSortAscending();
	String timelineFilterSearch();
	String timelineFilterSearchPlaceholder();
	String timelineDisclaimerOverlayContent();
	String timelineDisclaimerOverlayButton();

	String viewerSeriesInfo(int seriesNumber, int seriesSize);
	String viewerMultiFrameLoader();
	String viewerDownloadButton();
	String viewerDownloadDicomButton();
	String viewerDownloadDicomOverlayContent();
	String viewerDownloadDicomOverlayPosButton();
	String viewerDownloadDicomOverlayNegButton();
	String viewerNotFoundErrorOverlayContent();
	String viewerNotFoundErrorOverlayButton();
	String viewerGenericErrorOverlayContent();
	String viewerGenericErrorOverlayButton();
	String viewerTokenErrorOverlayContent();
	String viewerTokenErrorOverlayButton();
}
