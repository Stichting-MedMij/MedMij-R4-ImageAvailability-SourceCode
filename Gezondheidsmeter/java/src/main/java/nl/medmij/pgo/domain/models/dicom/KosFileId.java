// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.models.dicom;

public class KosFileId extends KosId {
	private final String fileName;

	private KosFileId(String fileName) {
		this.fileName = fileName;
	}

	public static KosFileId ofString(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}
		return new KosFileId(fileName);
	}

	@Override
	public String toString() {
		return fileName;
	}
}
