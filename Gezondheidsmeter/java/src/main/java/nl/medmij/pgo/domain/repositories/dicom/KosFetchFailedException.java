// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.repositories.dicom;

import nl.medmij.pgo.domain.models.dicom.KosId;

public class KosFetchFailedException extends Exception {
	private final KosId kosId;

	public KosFetchFailedException(KosId kosId, Throwable cause) {
		this.kosId = kosId;
	}

	public KosId getKosId() {
		return kosId;
	}
}
