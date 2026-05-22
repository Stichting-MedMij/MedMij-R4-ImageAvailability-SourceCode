// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;
import nl.medmij.pgo.util.JsonUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Series {
	private final Study study;
	private final String seriesInstanceId;
	private final String retrieveUrl;
	private final String retrieveLocationId;
	private final List<Instance> instances;

	/**
	 * Parse the provided json as a Series object.<br>Note: no checks are done with respect to the validity of this
	 * node. If a node is invalid the returned Series object might contain just {@code ""} (empty string) values.
	 * @param seriesObjectNode An {@link ObjectNode} embedded in an {@link ArrayNode} named {@code Value} in an
	 *                           {@link ObjectNode} named {@code "00081115"}.<br>Note: takes a {@link JsonNode}
	 *                           rather than {@link ObjectNode} as parameter for ease of use
	 * @param study The DICOM study this Series belongs to
	 */
	Series (JsonNode seriesObjectNode, Study study) {
		this.study = study;
		this.seriesInstanceId = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, seriesObjectNode.get(DicomCodes.SERIES_INSTANCE_UID), "");
		this.retrieveUrl = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, seriesObjectNode.get(DicomCodes.RETRIEVE_URL), "");
		this.retrieveLocationId = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, seriesObjectNode.get(DicomCodes.RETRIEVE_LOCATION_UID), "");
		this.instances = JsonUtil.getValuesAsList(seriesObjectNode.get(DicomCodes.REFERENCED_SOP_SEQUENCE))
				.stream()
				.filter(Objects::nonNull)
				.map(node -> new Instance(node, this))
				.collect(Collectors.toList());
	}

	public Study getStudy() {
		return study;
	}

	public String getSeriesInstanceId() {
		return seriesInstanceId;
	}

	public String getRetrieveUrl() {
		return retrieveUrl;
	}

	public String getRetrieveLocationId() {
		return retrieveLocationId;
	}

	public List<Instance> getInstances() {
		return instances
				.stream()
				.sorted(Comparator.comparing(Instance::getInstanceId))
				.collect(Collectors.toList());
	}
}
