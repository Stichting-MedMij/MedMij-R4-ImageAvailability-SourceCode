// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.repositories.dicom;

import nl.medmij.pgo.domain.models.dicom.Kos;
import nl.medmij.pgo.domain.models.dicom.KosId;

public interface DicomKosRepository {
	Kos fetch(KosId kosId) throws KosFetchFailedException;
}
