// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.medmij.pgo.constants.dicom.DicomCodes;
import nl.medmij.pgo.util.JsonUtil;

public class Instance {

	private final Series series;
	private final String instanceId;
	private final SopClass sopClass;
	private final String rawSopClass;

	/**
	 * Parse the provided json as an Instance object.<br>Note: no checks are done with respect to the validity of
	 * this node. If a node is invalid the returned Instance object might contain just {@code ""} (empty string)
	 * values
	 * @param instanceObjectNode An {@link ObjectNode} embedded in an {@link ArrayNode} named {@code Value} in an
	 *                              {@link ObjectNode} named {@code "00081199"}.<br>Note: takes a {@link JsonNode}
	 *                              rather than {@link ObjectNode} as parameter for ease of use
	 * @param series The DICOM series this Instance belongs to
	 */
	Instance(JsonNode instanceObjectNode, Series series) {
		this.series = series;
		this.instanceId = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, instanceObjectNode.get(DicomCodes.REFERENCED_SOP_INSTANCE_UID), "");
		this.rawSopClass = JsonUtil.getFirstValueInterpreted(JsonNode::textValue, instanceObjectNode.get(DicomCodes.REFERENCED_SOP_CLASS_UID), "");
		this.sopClass = SopClass.fromUID(rawSopClass);
	}


	public Series getSeries() {
		return series;
	}

	public String getInstanceId() {
		return instanceId;
	}
	public SopClass getSopClass() {
		return sopClass;
	}

	public String getRawSopClass() {
		return rawSopClass;
	}

	public boolean isSupported() {
		return sopClass != SopClass.UNKNOWN && !SopClass.STRUCTURED_REPORTS.contains(sopClass);
	}
}
