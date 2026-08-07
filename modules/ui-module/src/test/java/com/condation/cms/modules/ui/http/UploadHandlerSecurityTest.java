package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * UI Module
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UploadHandlerSecurityTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void createsAPrivateUploadWorkDirectoryOutsideThePublicOutputDirectory() throws Exception {
		Path outputDirectory = temporaryDirectory.resolve("assets");

		new UploadHandler("/upload", outputDirectory, false);

		Path workDirectory = temporaryDirectory.resolve(".condation-upload-work");
		assertThat(workDirectory).isDirectory();
		assertThat(workDirectory.toRealPath().getParent()).isEqualTo(temporaryDirectory.toRealPath());
		assertThat(workDirectory.startsWith(outputDirectory)).isFalse();
		if (Files.getFileStore(workDirectory).supportsFileAttributeView("posix")) {
			assertThat(Files.getPosixFilePermissions(workDirectory))
					.isEqualTo(PosixFilePermissions.fromString("rwx------"));
		}
	}

	@Test
	void stripsClientSidePathsFromUploadedFilenames() throws Exception {
		UploadHandler handler = new UploadHandler("/upload", temporaryDirectory.resolve("assets"), false);

		assertThat(handler.slugifyFilename("../../Bad Name.PNG")).isEqualTo("bad-name.png");
		assertThat(handler.slugifyFilename("..\\..\\Photo.JPEG")).isEqualTo("photo.jpeg");
		assertThatIOException()
				.isThrownBy(() -> handler.slugifyFilename("image.invalid-extension"));
	}
}
