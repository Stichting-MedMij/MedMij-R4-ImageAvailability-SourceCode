// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JsonUtil {

	private JsonUtil() {

	}

	/**
	 * Get the first valid value from the parent node interpreted using the supplied interpretation function
	 * @param interpretationFunc interpretation function, if an error occurs for a certain node, that node is treated as invalid and is skipped
	 * @param parentNode An {@link ObjectNode} containing an {@link ArrayNode} named "Value"
	 * @param defaultValue default value to be returned in case the provided parent node doesn't meet the requirements or no valid value is found
	 */
	public static <T> T getFirstValueInterpreted(Function<JsonNode, T> interpretationFunc, JsonNode parentNode, T defaultValue) {
		try {
			return getValuesAsList(parentNode)
					.stream()
					.map(node -> applyInterpretationFunc(node, interpretationFunc))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst()
					.orElse(defaultValue);
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	private static <T> Optional<T> applyInterpretationFunc(JsonNode node, Function<JsonNode, T> interpretationFunc) {
		try {
			return Optional.of(interpretationFunc.apply(node));
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * Get the values in the provided parent node as a list
	 * @param parentNode An {@link ObjectNode} containing an {@link ArrayNode} named "Value"
	 */
	public static List<JsonNode> getValuesAsList(JsonNode parentNode) {
		try {
			List<JsonNode> ret = new ArrayList<>();
			parentNode.get("Value").elements().forEachRemaining(ret::add);
			return ret;
		} catch (NullPointerException e) {
			return Collections.emptyList();
		}
	}
}
