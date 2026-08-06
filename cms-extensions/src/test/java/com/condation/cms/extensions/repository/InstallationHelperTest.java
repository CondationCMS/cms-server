package com.condation.cms.extensions.repository;

/*-
 * #%L
 * CMS Extensions
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InstallationHelperTest {

	@TempDir
	private Path tempDir;

	@Test
	void unpackArchiveExtractsEntriesInsideTargetDirectory() throws Exception {
		Path archive = createArchive("example/", "example/readme.txt");
		Path target = tempDir.resolve("target");

		var moduleDirectory = InstallationHelper.unpackArchive(archive.toFile(), target.toFile());

		Assertions.assertThat(moduleDirectory).isEqualTo(target.resolve("example").toFile());
		Assertions.assertThat(target.resolve("example/readme.txt"))
				.hasContent("content");
	}

	@Test
	void unpackArchiveRejectsEntriesOutsideTargetDirectory() throws Exception {
		Path archive = createArchive("example/", "../../escaped.txt");
		Path target = tempDir.resolve("installation/target");

		Assertions.assertThatThrownBy(() -> InstallationHelper.unpackArchive(archive.toFile(), target.toFile()))
				.isInstanceOf(InstallationSecurityException.class)
				.hasMessageContaining("escapes target directory");
		Assertions.assertThat(tempDir.resolve("escaped.txt")).doesNotExist();
	}

	private Path createArchive(String directoryEntry, String fileEntry) throws Exception {
		Path archive = tempDir.resolve("extension-%d.zip".formatted(System.nanoTime()));
		try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
			zip.putNextEntry(new ZipEntry(directoryEntry));
			zip.closeEntry();
			zip.putNextEntry(new ZipEntry(fileEntry));
			zip.write("content".getBytes());
			zip.closeEntry();
		}
		return archive;
	}
}
