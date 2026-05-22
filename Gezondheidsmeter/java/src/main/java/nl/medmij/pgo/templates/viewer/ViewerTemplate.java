// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.templates.viewer;

import nl.medmij.pgo.domain.models.dicom.Instance;
import nl.medmij.pgo.domain.models.dicom.Kos;
import nl.medmij.pgo.domain.models.dicom.Series;
import nl.medmij.pgo.domain.models.dicom.Study;
import nl.medmij.pgo.templates.Translations;

import java.util.List;
import java.util.stream.Collectors;

public class ViewerTemplate {

	private ViewerTemplate() {
		// Remove implicit public constructor
	}

	public static String generateViewerHtml(Translations translations, Kos kos, String backendImagesBaseUrl) {
		StringBuilder html = new StringBuilder();
		html.append("<div id=\"bbs-viewer\">\n");
		html.append("\t<div class=\"medmij-dicom-viewer\" data-medmij-kos-id=\"").append(kos.getId()).append("\" data-medmij-img-endpoint=\"").append(backendImagesBaseUrl).append("\">\n");
		html.append("\t\t<div class=\"study\">\n");
		html.append(generateStudyHeader());
		html.append(generateStudyContent(translations, kos));
		html.append(generateStudyFooter(translations, kos));
		html.append("\t\t</div>\n");
		html.append("\t\t<div class=\"series hidden\">\n");
		html.append(generateSeriesHeader());
		html.append(generateSeriesContent(translations));
		html.append(generateSeriesFooter(translations, kos));
		html.append(generateDownloadDicomConfirmOverlay(translations));
		html.append("\t\t</div>\n");
		html.append("\t</div>\n");
		html.append(generateOverlay(
				"not-found-error",
				translations.viewerNotFoundErrorOverlayContent(),
				translations.viewerNotFoundErrorOverlayButton()
		));
		html.append(generateOverlay(
				"generic-error",
				translations.viewerGenericErrorOverlayContent(),
				translations.viewerGenericErrorOverlayButton()
		));
		html.append(generateOverlay(
				"token-error",
				translations.viewerTokenErrorOverlayContent(),
				translations.viewerTokenErrorOverlayButton()
		));
		html.append("</div>\n");

		return html.toString();
	}

	private static String generateStudyHeader() {
		// TODO: probably use generateSeriesHeader instead and hide the fields that are not relevant for the study view
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"nav\">\n");
		html.append("\t\t\t\t<button class=\"close\"></button>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateStudyContent(Translations translations, Kos kos) {
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"contents\">\n");
		html.append("\t\t\t\t<div class=\"series-list\">\n");
		for (int i = 0; i < kos.getSeries().size(); i++) {
			Series series = kos.getSeries().get(i);
			List<Instance> instances = series.getInstances().stream().filter(Instance::isSupported).collect(Collectors.toList());
			if (! instances.isEmpty()) {
				html.append("\t\t\t\t\t<button class=\"series\" data-medmij-series-id=\"").append(series.getSeriesInstanceId()).append("\" data-medmij-series-len=\"").append(instances.size()).append("\">\n");
				html.append("\t\t\t\t\t\t<h3 class=\"h3\">").append(translations.viewerSeriesInfo(i + 1, series.getInstances().size())).append("</h3>\n");
				for (Instance instance : instances) {
					html.append("\t\t\t\t\t\t<template class=\"instance-info\" data-medmij-instance-id=\"").append(instance.getInstanceId()).append("\"></template>\n");
				}
				html.append("\t\t\t\t\t</button>\n");
			}
		}
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateStudyFooter(Translations translations, Kos kos) {
		// TODO: probably use generateSeriesFooter instead and hide the fields that are not relevant for the study view
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"image-controls\">\n");
		html.append("\t\t\t\t<div class=\"zoom\"></div>\n");
		html.append("\t\t\t\t<div class=\"info\">\n");
		html.append("\t\t\t\t\t<div class=\"hcp-name\">").append(kos.getStudies().stream().findAny().map(Study::getHcpName).orElse("")).append("</div>\n");
		html.append("\t\t\t\t\t<div class=\"date\">").append(kos.getStudies().stream().findAny().map(s -> translations.date(s.getStudyDate())).orElse("")).append("</div>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t\t<div class=\"download\"></div>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateSeriesHeader() {
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"nav\">\n");
		html.append("\t\t\t\t<button class=\"back\"></button>\n");
		html.append("\t\t\t\t<div class=\"gallery-controls\">\n");
		html.append("\t\t\t\t\t<button class=\"prev\"></button>\n");
		html.append("\t\t\t\t\t<div class=\"index\"></div>\n");
		html.append("\t\t\t\t\t<button class=\"next\"></button>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t\t<button class=\"close\"></button>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateSeriesContent(Translations translations) {
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"contents\">\n");
		html.append("\t\t\t\t<div class=\"multiframe-loader hidden\">\n");
		html.append("\t\t\t\t\t<span class=\"static-text\">").append(translations.viewerMultiFrameLoader()).append("</span>\n");
		html.append("\t\t\t\t\t<br>\n");
		html.append("\t\t\t\t\t<span class=\"progress\"></span>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t\t<img class=\"hidden\">\n");
		html.append("\t\t\t\t<iframe class=\"hidden\"></iframe>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateSeriesFooter(Translations translations, Kos kos) {
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div class=\"image-controls\">\n");
		html.append("\t\t\t\t<div class=\"zoom\">\n");
		html.append("\t\t\t\t\t<button class=\"minus\"></button>\n");
		html.append("\t\t\t\t\t<input type=\"range\" class=\"slider\" min=\"1\" max=\"100\" step=\"1\" value=\"1\">\n");
		html.append("\t\t\t\t\t<button class=\"plus\"></button>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t\t<div class=\"info\">\n");
		html.append("\t\t\t\t\t<div class=\"hcp-name\">").append(kos.getStudies().stream().findAny().map(Study::getHcpName).orElse("")).append("</div>\n");
		html.append("\t\t\t\t\t<div class=\"date\">").append(kos.getStudies().stream().findAny().map(s -> translations.date(s.getStudyDate())).orElse("")).append("</div>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t\t<div class=\"download\">\n");
		html.append("\t\t\t\t\t<button class=\"image\">").append(translations.viewerDownloadButton()).append("</button>\n");
		html.append("\t\t\t\t\t<button class=\"dicom\">").append(translations.viewerDownloadDicomButton()).append("</button>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateDownloadDicomConfirmOverlay(Translations translations) {
		StringBuilder html = new StringBuilder();
		html.append("\t\t\t<div id=\"dicom-confirm-overlay\" class=\"overlay beeld-confirm hidden\">\n");
		html.append("\t\t\t\t<div class=\"overlay-content\">\n");
		html.append("\t\t\t\t\t<div class=\"overlay-inner\">\n");
		html.append("\t\t\t\t\t\t<div class=\"group\">").append(translations.viewerDownloadDicomOverlayContent()).append("</div>\n");
		html.append("\t\t\t\t\t\t<button type=\"button\" class=\"primary\">").append(translations.viewerDownloadDicomOverlayPosButton()).append("</button>\n");
		html.append("\t\t\t\t\t\t<button type=\"button\" class=\"secondary\">").append(translations.viewerDownloadDicomOverlayNegButton()).append("</button>\n");
		html.append("\t\t\t\t\t</div>\n");
		html.append("\t\t\t\t</div>\n");
		html.append("\t\t\t</div>\n");
		return html.toString();
	}

	private static String generateOverlay(String id, String descText, String btnText) {
		StringBuilder html = new StringBuilder();
		html.append("\t<div id=\"").append(id).append("\" class=\"overlay beeld-confirm hidden\">\n");
		html.append("\t\t<div class=\"overlay-content\">\n");
		html.append("\t\t\t<div class=\"overlay-inner\">\n");
		html.append("\t\t\t\t<div class=\"group\">").append(descText).append("</div>\n");
		html.append("\t\t\t\t<button type=\"button\" class=\"primary\" onclick=\"document.getElementById('").append(id).append("').classList.toggle('hidden');\">").append(btnText).append("</button>\n");
		html.append("\t\t\t</div>\n");
		html.append("\t\t</div>\n");
		html.append("\t</div>\n");
		return html.toString();
	}
}
