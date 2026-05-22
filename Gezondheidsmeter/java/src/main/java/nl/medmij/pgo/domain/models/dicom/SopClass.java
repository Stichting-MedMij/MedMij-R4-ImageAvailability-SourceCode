// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes what type of data an {@link Instance} refers to
 */
public enum SopClass {
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Computed Radiography (CR) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Digitalized conventional X-ray images, often used in older systems.</td>
	 *     </tr>
	 * </table>
	 */
	CR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Digital X-Ray Image Storage – For Presentation</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.1.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Modern digital X-ray images, successor of Computed Radiography (CR) Image Storage.</td>
	 *     </tr>
	 * </table>
	 */
	DX,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Digital Mammography X-Ray Image Storage – For Presentation</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.1.2</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Specialized X-ray images for digital mammography.</td>
	 *     </tr>
	 * </table>
	 */
	MG,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Computed Tomography (CT) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.2</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Standard CT images.</td>
	 *     </tr>
	 * </table>
	 */
	CT,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Enhanced Computed Tomography (CT) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.2.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Enhanced CT images with multi-frame structure, recommended to be future-proof.</td>
	 *     </tr>
	 * </table>
	 */
	CT_MF,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Ultrasound Multi-frame Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.3.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Dynamic ultrasound images (cine-loops).</td>
	 *     </tr>
	 * </table>
	 */
	US_MF,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Magnetic Resonance (MR) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.4</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Standard MRI images, supported by all systems.</td>
	 *     </tr>
	 * </table>
	 */
	MR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Enhanced Magnetic Resonance (MR) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.4.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Multi-frame MRI images with extensive metadata, used by modern MRI scanners.</td>
	 *     </tr>
	 * </table>
	 */
	MR_MF,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Ultrasound Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.6.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Static 2D ultrasound images, often used in almost all ultrasound examinations.</td>
	 *     </tr>
	 * </table>
	 */
	US,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Secondary Capture Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.7</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Digital photos or screenshots, e.g. from non-DICOM devices.</td>
	 *     </tr>
	 * </table>
	 */
	SC,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>X-Ray Angiographic Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.12.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Angiographic images.</td>
	 *     </tr>
	 * </table>
	 */
	XA,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>X-Ray Radiofluoroscopic Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.12.2</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Dynamic X-ray images, such as swallow study videos.</td>
	 *     </tr>
	 * </table>
	 */
	RF,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Nuclear Medicine Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.20</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Images of gamma cameras used in nuclear medicine (not in radiology), important for functional imaging (e.g. thyroid, skeleton).</td>
	 *     </tr>
	 * </table>
	 */
	NM,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Video Endoscopic Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.77.1.1.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Endoscopic images.</td>
	 *     </tr>
	 * </table>
	 */
	ES,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Encapsulated PDF Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.104.1</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Used to store PDF documents as DICOM objects, e.g. reports and attachments.</td>
	 *     </tr>
	 * </table>
	 */
	OT,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Positron Emission Tomography (PET) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.128</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>PET scan images used in nuclear medicine.</td>
	 *     </tr>
	 * </table>
	 */
	PT,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class Name</td>
	 *         <td>Enhanced Positron Emission Tomography (PET) Image Storage</td>
	 *     </tr>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.130</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Enhanced PET scan images used in nuclear medicine.</td>
	 *     </tr>
	 * </table>
	 */
	EPT,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.11</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Basic Text SR</td>
	 *     </tr>
	 * </table>
	 */
	BASIC_TEXT_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.22</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Enhanced SR</td>
	 *     </tr>
	 * </table>
	 */
	ENHANCED_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.33</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Comprehensive SR</td>
	 *     </tr>
	 * </table>
	 */
	COMPREHENSIVE_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.34</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Comprehensive 3D SR</td>
	 *     </tr>
	 * </table>
	 */
	COMPREHENSIVE_3D_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.40</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Procedure Log</td>
	 *     </tr>
	 * </table>
	 */
	PROCEDURE_LOG,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.50</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Mammography CAD SR</td>
	 *     </tr>
	 * </table>
	 */
	MAMMOGRAPHY_CAD_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.59</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Key Object Selection Document</td>
	 *     </tr>
	 * </table>
	 */
	KEY_OBJECT_SELECTION_DOCUMENT,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.65</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Chest CAD SR</td>
	 *     </tr>
	 * </table>
	 */
	CHEST_CAD_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.67</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>X-Ray Radiation Dose SR</td>
	 *     </tr>
	 * </table>
	 */
	XRAY_RADIATION_DOSE_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.69</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Colon CAD SR</td>
	 *     </tr>
	 * </table>
	 */
	COLON_CAD_SR,
	/**
	 * <table>
	 *     <tr>
	 *         <td>SOP Class UID</td>
	 *         <td>1.2.840.10008.5.1.4.1.1.88.70</td>
	 *     </tr>
	 *     <tr>
	 *         <td>Description</td>
	 *         <td>Implantation Plan SR Document Storage</td>
	 *     </tr>
	 * </table>
	 */
	IMPLANTATION_PLAN_SR_DOCUMENT_STORAGE,
	UNKNOWN;

	public static final Set<SopClass> IMAGES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			CR,
			DX,
			MG,
			CT,
			MR,
			US,
			SC,
			NM,
			PT,
			EPT
	)));
	public static final Set<SopClass> MULTI_FRAME_IMAGES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			CT_MF,
			US_MF,
			MR_MF,
			XA
	)));
	public static final Set<SopClass> VIDEO = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			RF,
			ES
	)));
	public static final Set<SopClass> PDF = Collections.singleton(OT);
	public static final Set<SopClass> STRUCTURED_REPORTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			BASIC_TEXT_SR,
			ENHANCED_SR,
			COMPREHENSIVE_SR,
			COMPREHENSIVE_3D_SR,
			PROCEDURE_LOG,
			MAMMOGRAPHY_CAD_SR,
			KEY_OBJECT_SELECTION_DOCUMENT,
			CHEST_CAD_SR,
			XRAY_RADIATION_DOSE_SR,
			COLON_CAD_SR,
			IMPLANTATION_PLAN_SR_DOCUMENT_STORAGE
	)));

	static SopClass fromUID(String uid) {
		switch (uid) {
			case "1.2.840.10008.5.1.4.1.1.1": return CR;
			case "1.2.840.10008.5.1.4.1.1.1.1": return DX;
			case "1.2.840.10008.5.1.4.1.1.1.2": return MG;
			case "1.2.840.10008.5.1.4.1.1.2": return CT;
			case "1.2.840.10008.5.1.4.1.1.2.1": return CT_MF;
			case "1.2.840.10008.5.1.4.1.1.3.1": return US_MF;
			case "1.2.840.10008.5.1.4.1.1.4": return MR;
			case "1.2.840.10008.5.1.4.1.1.4.1": return MR_MF;
			case "1.2.840.10008.5.1.4.1.1.6.1": return US;
			case "1.2.840.10008.5.1.4.1.1.7": return SC;
			case "1.2.840.10008.5.1.4.1.1.12.1": return XA;
			case "1.2.840.10008.5.1.4.1.1.12.2": return RF;
			case "1.2.840.10008.5.1.4.1.1.20": return NM;
			case "1.2.840.10008.5.1.4.1.1.77.1.1.1": return ES;
			case "1.2.840.10008.5.1.4.1.1.104.1": return OT;
			case "1.2.840.10008.5.1.4.1.1.128": return PT;
			case "1.2.840.10008.5.1.4.1.1.130": return EPT;
			case "1.2.840.10008.5.1.4.1.1.88.11": return BASIC_TEXT_SR;
			case "1.2.840.10008.5.1.4.1.1.88.22": return ENHANCED_SR;
			case "1.2.840.10008.5.1.4.1.1.88.33": return COMPREHENSIVE_SR;
			case "1.2.840.10008.5.1.4.1.1.88.34": return COMPREHENSIVE_3D_SR;
			case "1.2.840.10008.5.1.4.1.1.88.40": return PROCEDURE_LOG;
			case "1.2.840.10008.5.1.4.1.1.88.50": return MAMMOGRAPHY_CAD_SR;
			case "1.2.840.10008.5.1.4.1.1.88.59": return KEY_OBJECT_SELECTION_DOCUMENT;
			case "1.2.840.10008.5.1.4.1.1.88.65": return CHEST_CAD_SR;
			case "1.2.840.10008.5.1.4.1.1.88.67": return XRAY_RADIATION_DOSE_SR;
			case "1.2.840.10008.5.1.4.1.1.88.69": return COLON_CAD_SR;
			case "1.2.840.10008.5.1.4.1.1.88.70": return IMPLANTATION_PLAN_SR_DOCUMENT_STORAGE;
			default: return UNKNOWN;
		}
	}
}
