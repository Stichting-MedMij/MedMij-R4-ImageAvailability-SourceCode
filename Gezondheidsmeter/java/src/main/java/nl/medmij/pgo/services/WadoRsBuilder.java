// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.services;

import nl.medmij.pgo.domain.models.dicom.Instance;
import nl.medmij.pgo.domain.models.dicom.Series;
import nl.medmij.pgo.domain.models.dicom.Study;

import java.net.URI;
import java.util.Objects;

/**
 * Builder class to generate the <a href="https://www.dicomstandard.org/using/dicomweb/retrieve-wado-rs-and-wado-uri">
 * WADO-RS</a> urls. The {@code "bulkdata" and frames} endpoints are not included, because currently there is no
 * usecase for them. Note that not all of these might be supported by a DVA
 */
public class WadoRsBuilder {
	public enum Endpoint {
		BASIC (""),
		RENDERED ("/rendered"),
		METADATA ("/metadata");

		private final String endpoint;

		Endpoint(String endpoint) {
			this.endpoint = endpoint;
		}
	}

	private final URI base;
	private String studyInstanceId = "";
	private String seriesInstanceId = "";
	private String instanceId = "";
	private String frameNumber = "";
	private Endpoint endpoint = Endpoint.BASIC;

	/**
	 * Create a new {@code WadoRsBuilder} with the given base. Note: we consider the retrieve
	 * location id that some parties might additionally provide as part of the base
	 */
	public WadoRsBuilder(URI base) {
		this.base = base;
	}

	/**
	 * Attempts to make a {@code WadoRsBuilder} from the provided series
	 * @throws IllegalArgumentException if the retrieve url from the provided series does not contain a valid WADO-RS url
	 */
	public static WadoRsBuilder fromSeries(Series series) throws IllegalArgumentException {
		if (! series.getRetrieveUrl().contains("/studies/") || ! series.getRetrieveUrl().contains("/series/")) {
			// Not a valid WADO-RS url, so we'll assume we can't make a builder with the information provided in the series
			throw new IllegalArgumentException("Series retrieve url not usable");
		}
		String[] components = series.getRetrieveUrl().split("/studies/");
		URI base = URI.create(components[0]);
		WadoRsBuilder builder = new WadoRsBuilder(base).series(series);
		return builder;
	}

	/**
	 * Attempts to make a {@code WadoRsBuilder} from the provided series
	 * @throws IllegalArgumentException if the retrieve url from series the provided instnace belongs to does not contain a valid WADO-RS url
	 */
	public static WadoRsBuilder fromInstance(Instance instance) throws IllegalArgumentException {
		return fromSeries(instance.getSeries()).instance(instance);
	}

	public URI getBase() {
		return base;
	}

	public String getStudyInstanceId() {
		return studyInstanceId;
	}

	public String getSeriesInstanceId() {
		return seriesInstanceId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getFrameNumber() {
		return frameNumber;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	/**
	 * Create a copy of {@code this WadoRsBuilder}
	 * @return a new {@code WadoRsBuilder} instance with the same values as {@code this WadoRsBuilder}
	 */
	public WadoRsBuilder copy() {
		WadoRsBuilder copy = new WadoRsBuilder(this.base);
		copy.studyInstanceId = this.studyInstanceId;
		copy.seriesInstanceId = this.seriesInstanceId;
		copy.instanceId = this.instanceId;
		copy.endpoint = this.endpoint;
		return copy;
	}

	/**
	 * Sets the study id of {@code this WadoRsBuilder} to match the id of the provided {@link Study}
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder study(Study study) {
		this.studyInstanceId = study.getStudyInstanceId();
		return this;
	}

	/**
	 * Sets the study id of {@code this WadoRsBuilder} to the provided String
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder studyInstanceId(String studyInstanceId) {
		this.studyInstanceId = studyInstanceId;
		return this;
	}

	/**
	 * Sets the study and series ids of {@code this WadoRsBuilder} to match the ids of the provided {@link Series}
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder series(Series series) {
		this.studyInstanceId = series.getStudy().getStudyInstanceId();
		this.seriesInstanceId = series.getSeriesInstanceId();
		return this;
	}

	/**
	 * Sets the series id of {@code this WadoRsBuilder} to the provided String
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder seriesInstanceId(String seriesInstanceId) {
		this.seriesInstanceId = seriesInstanceId;
		return this;
	}

	/**
	 * Sets the study, series and instance ids of {@code this WadoRsBuilder} to match the ids of the provided {@link Instance}
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder instance(Instance instance) {
		this.studyInstanceId = instance.getSeries().getStudy().getStudyInstanceId();
		this.seriesInstanceId = instance.getSeries().getSeriesInstanceId();
		this.instanceId = instance.getInstanceId();
		return this;
	}

	/**
	 * Sets the instance id of {@code this WadoRsBuilder} to the provided String
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder instanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	/**
	 * Sets the instance id of {@code this WadoRsBuilder} to the provided String
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder frameNumber(String frameNumber) {
		this.frameNumber = frameNumber;
		return this;
	}

	/**
	 * Sets the endpoint of {@code this WadoRsBuilder} to the provided {@link Endpoint}
	 * @return {@code this WadoRsBuilder} for chaining
	 */
	public WadoRsBuilder endpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	private String buildStudyUrl() {
		if (studyInstanceId.isEmpty()) {
			throw new IllegalArgumentException("No study id supplied");
		}
		String addSlash = base.toString().endsWith("/") ? "" : "/";
		return base + addSlash + "studies/" + studyInstanceId;
	}

	/**
	 * Generate the WADO-RS study url for the values set in {@code this WadoRsBuilder}
	 * @return {@link URI} object for the generated url
	 * @throws IllegalArgumentException if study id is missing
	 */
	public URI generateStudyUrl() {
		return URI.create(buildStudyUrl() + endpoint.endpoint);
	}

	private String buildSeriesUrl() {
		if (seriesInstanceId.isEmpty()) {
			throw new IllegalArgumentException("No series id supplied");
		}
		return buildStudyUrl() + "/series/" + seriesInstanceId;
	}

	/**
	 * Generate the WADO-RS series url for the values set in {@code this WadoRsBuilder}
	 * @return {@link URI} object for the generated url
	 * @throws IllegalArgumentException if study or series id is missing
	 */
	public URI generateSeriesUrl() {
		return URI.create(buildSeriesUrl() + endpoint.endpoint);
	}

	private String buildInstancesUrl() {
		if (instanceId.isEmpty()) {
			throw new IllegalArgumentException("No instance id supplied");
		}
		return buildSeriesUrl() + "/instances/" + instanceId;
	}

	/**
	 * Generate the WADO-RS instances url for the values set in {@code this WadoRsBuilder}
	 * @return {@link URI} object for the generated url
	 * @throws IllegalArgumentException if study, series or instance id is missing
	 */
	public URI generateInstancesUrl() {
		return URI.create(buildInstancesUrl() + endpoint.endpoint);
	}

	private String buildFramesUrl() {
		if (frameNumber.isEmpty()) {
			throw new IllegalArgumentException("No frame number supplied");
		}
		return buildInstancesUrl() + "/frames/" + frameNumber;
	}

	/**
	 * Generate the WADO-RS instances url for the values set in {@code this WadoRsBuilder}
	 * @return {@link URI} object for the generated url
	 * @throws IllegalArgumentException if study, series or instance id or frame number is missing
	 */
	public URI generateFramesUrl() {
		return URI.create(buildFramesUrl() + endpoint.endpoint);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WadoRsBuilder that = (WadoRsBuilder) o;
		return Objects.equals(base, that.base) && Objects.equals(studyInstanceId, that.studyInstanceId) && Objects.equals(seriesInstanceId, that.seriesInstanceId) && Objects.equals(instanceId, that.instanceId) && endpoint == that.endpoint;
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, studyInstanceId, seriesInstanceId, instanceId, endpoint);
	}
}
