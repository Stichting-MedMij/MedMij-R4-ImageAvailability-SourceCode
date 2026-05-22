// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista
package nl.medmij.pgo.domain.repositories.dicom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.medmij.pgo.domain.models.dicom.Kos;
import nl.medmij.pgo.domain.models.dicom.KosId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DicomKosRepositoryFileImpl implements DicomKosRepository {
	private final String baseDir;

	public DicomKosRepositoryFileImpl(String baseDir) {
		this.baseDir = baseDir.endsWith("/") ? baseDir : baseDir + "/";
	}

	@Override
	public Kos fetch(KosId kosId) throws KosFetchFailedException {
		try {
			File kosFile = Paths.get(baseDir + kosId).toFile();
			if (kosFile.isFile()) {
				try (InputStream input = Files.newInputStream(kosFile.toPath())) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.readTree(input);
					return Kos.fromJson(node, kosId);
				}
			}
			throw new FileNotFoundException();
		} catch (IOException e) {
			throw new KosFetchFailedException(kosId, e);
		}
	}
}
