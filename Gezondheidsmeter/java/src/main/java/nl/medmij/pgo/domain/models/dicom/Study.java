// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;
import nl.medmij.pgo.util.JsonUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Study {
	private final String studyInstanceId;
	private final LocalDate studyDate;
	private final String hcpName;
	private final List<Series> series;

	/**
	 * Parse the provided json as a Study object.<br>Note: no checks are done with respect to the validity of this
	 * node. If a node is invalid the returned Study object might contain just {@code ""} (empty string) values
	 * @param studyObjectNode An {@link ObjectNode} that's part of the top level {@link ArrayNode}. Note: takes a
	 *                          {@link JsonNode} rather than {@link ObjectNode} as parameter for ease of use
	 */
	Study(JsonNode studyObjectNode) {
		this.studyInstanceId = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, studyObjectNode.get(DicomCodes.STUDY_INSTANCE_UID), "");
		String studyDateStr = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, studyObjectNode.get(DicomCodes.STUDY_DATE), "");
		this.studyDate = LocalDate.parse(studyDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
		this.hcpName = JsonUtil.getFirstValueInterpreted(node -> node.get("Alphabetic").textValue(), studyObjectNode.get(DicomCodes.REFERRING_PHYSICIANS_NAME), "");
		Optional<JsonNode> crpesNode = JsonUtil.getValuesAsList(studyObjectNode.get(DicomCodes.CURRENT_REQUESTED_PROCEDURE_EVIDENCE_SEQUENCE))
				.stream()
				// Following the specs there should be precisely 1, so just get the first, but be lenient about it in case there's none
				.findFirst();

		this.series = crpesNode.map(crpesN ->
				JsonUtil.getValuesAsList(crpesN.get(DicomCodes.REFERENCED_SERIES_SEQUENCE))
						.stream()
						.filter(Objects::nonNull)
						.map(n -> new Series(n, this))
						.collect(Collectors.toList())
		).orElseGet(Collections::emptyList);
	}

	public String getStudyInstanceId() {
		return studyInstanceId;
	}

	public LocalDate getStudyDate() {
		return studyDate;
	}

	public String getHcpName() {
		return hcpName;
	}

	public List<Series> getSeries() {
		return series;
	}

	/**
	 * Get a list of all Instances that belong to Series that belong to {@code this} Study
	 */
	public List<Instance> getInstances() {
		List<Instance> instances = new ArrayList<>();
		series.forEach(s -> instances.addAll(s.getInstances()));
		return Collections.unmodifiableList(instances);
	}
}
