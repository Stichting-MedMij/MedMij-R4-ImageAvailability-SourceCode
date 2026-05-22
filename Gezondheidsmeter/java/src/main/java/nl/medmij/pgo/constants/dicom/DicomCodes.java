// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.constants.dicom;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DicomCodes {

	public static final Map<String, Charset> DICOM_CHARSET_MAP;

	static {
		Map<String, Charset> dicomCharsetMap = new HashMap<>();
		dicomCharsetMap.put("ISO_IR 6", StandardCharsets.US_ASCII);
		dicomCharsetMap.put("ISO_IR 100", StandardCharsets.ISO_8859_1);
		dicomCharsetMap.put("ISO_IR 101", Charset.forName("ISO-8859-2"));
		dicomCharsetMap.put("ISO_IR 109", Charset.forName("ISO-8859-3"));
		dicomCharsetMap.put("ISO_IR 110", Charset.forName("ISO-8859-4"));
		dicomCharsetMap.put("ISO_IR 144", Charset.forName("ISO-8859-5"));
		dicomCharsetMap.put("ISO_IR 127", Charset.forName("ISO-8859-6"));
		dicomCharsetMap.put("ISO_IR 126", Charset.forName("ISO-8859-7"));
		dicomCharsetMap.put("ISO_IR 138", Charset.forName("ISO-8859-8"));
		dicomCharsetMap.put("ISO_IR 13", Charset.forName("Shift_JIS"));
		dicomCharsetMap.put("ISO_IR 166", Charset.forName("TIS-620"));
		dicomCharsetMap.put("ISO_IR 192", StandardCharsets.UTF_8);
		dicomCharsetMap.put("GB18030", Charset.forName("GB18030"));
		dicomCharsetMap.put("ISO 2022 IR 87", Charset.forName("ISO-2022-JP"));
		dicomCharsetMap.put("ISO 2022 IR 149", Charset.forName("ISO-2022-KR"));
		DICOM_CHARSET_MAP = Collections.unmodifiableMap(dicomCharsetMap);
	}

	public static final String SPECIFIC_CHARACTER_SET = "00080005";
	public static final String STUDY_DATE= "00080020";
	public static final String CONTENT_DATE = "00080023";
	public static final String CONTENT_TIME = "00080033";
	public static final String ACCESSION_NUMBER = "00080050";
	public static final String MODALITY = "00080060";
	public static final String REFERRING_PHYSICIANS_NAME = "00080090";
	public static final String REFERENCED_STUDY_SEQUENCE = "00081110";
	public static final String REFERENCED_STUDY_COMPONENT_SEQUENCE = "00081111";
	public static final String REFERENCED_SERIES_SEQUENCE = "00081115";
	public static final String RETRIEVE_URL = "00081190";
	public static final String REFERENCED_SOP_SEQUENCE = "00081199";
	public static final String REFERENCED_SOP_CLASS_UID = "00081150";
	public static final String REFERENCED_SOP_INSTANCE_UID = "00081155";
	public static final String STUDY_INSTANCE_UID = "0020000D";
	public static final String SERIES_INSTANCE_UID = "0020000E";
	public static final String SERIES_NUMBER = "00200011";
	public static final String INSTANCE_NUMBER = "00200013";
	public static final String NUMBER_OF_FRAMES = "00280008";
	public static final String REQUESTED_PROCEDURE_DESCRIPTION = "00321060";
	public static final String REQUESTED_PROCEDURE_CODE_SEQUENCE = "00321064";
	public static final String REQUESTED_PROCEDURE_ID = "00401001";
	public static final String PLACER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST = "00402016";
	public static final String FILLER_ORDER_NUMBER_IMAGING_SERVICE_REQUEST = "00402017";
	public static final String CONCEPT_NAME_CODE_SEQUENCE = "0040A043";
	public static final String REFERENCED_REQUEST_SEQUENCE = "0040A370";
	public static final String CURRENT_REQUESTED_PROCEDURE_EVIDENCE_SEQUENCE = "0040A375";
	public static final String CONTENT_SEQUENCE = "0040A730";
	public static final String RETRIEVE_LOCATION_UID = "0040E011";
	public static final String ENCPASULATED_DOCUMENT = "00420011";
	public static final String MIME_TYPE_OF_ENCPASULATED_DOCUMENT = "00420012";

	private DicomCodes() {
		// Remove implicit public constructor
	}
}
