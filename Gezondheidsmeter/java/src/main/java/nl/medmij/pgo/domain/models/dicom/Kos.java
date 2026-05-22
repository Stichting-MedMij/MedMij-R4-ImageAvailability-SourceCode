// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Kos {

	private final KosId id;
	private final List<Study> studies;

	private Kos(KosId id, List<Study> studies) {
		this.id = id;
		// Note: not making a defensive copy of this list, because it's made internally and as
		// such we can make sure it's not changed after being used in this constructor
		this.studies = Collections.unmodifiableList(studies);
	}

	public static Kos fromJson(JsonNode jsonNode, KosId kosId) {
		List<Study> studies = new ArrayList<>();
		jsonNode.elements().forEachRemaining(json -> studies.add(new Study(json)));
		return new Kos(kosId, studies);
	}

	public KosId getId() {
		return id;
	}

	public List<Study> getStudies() {
		return studies;
	}

	public List<Series> getSeries() {
		List<Series> series = new ArrayList<>();
		studies.forEach(s -> series.addAll(s.getSeries()));
		return series;
	}

	public List<Instance> getInstances() {
		List<Instance> instances = new ArrayList<>();
		// Reaching inside Series (rather than using Study.getInstances()) to prevent unnecessary List creations
		studies.forEach(st -> st.getSeries().forEach(se -> instances.addAll(se.getInstances())));
		return Collections.unmodifiableList(instances);
	}
}
