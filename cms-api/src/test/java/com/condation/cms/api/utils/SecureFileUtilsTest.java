package com.condation.cms.api.utils;

/*-
 * #%L
 * CMS Api
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecureFileUtilsTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void createsPrivateWorkDirectoriesAndFilesBelowTheConfiguredParent() throws Exception {
		Path workRoot = temporaryDirectory.resolve("work");

		Path workDirectory = SecureFileUtils.createPrivateTempDirectory(workRoot, "job-");
		Path workFile = SecureFileUtils.createPrivateTempFile(workDirectory, "upload-", ".tmp");

		assertThat(workDirectory).isDirectory().isDirectoryContaining(path -> path.equals(workFile));
		assertThat(workDirectory.getParent()).isEqualTo(workRoot.toRealPath());
		if (Files.getFileStore(workDirectory).supportsFileAttributeView("posix")) {
			assertThat(Files.getPosixFilePermissions(workDirectory))
					.isEqualTo(PosixFilePermissions.fromString("rwx------"));
			assertThat(Files.getPosixFilePermissions(workFile))
					.isEqualTo(PosixFilePermissions.fromString("rw-------"));
		} else if (Files.getFileStore(workDirectory).supportsFileAttributeView("acl")) {
			assertOwnerOnlyAcl(workRoot);
			assertOwnerOnlyAcl(workDirectory);
			assertOwnerOnlyAcl(workFile);
		}
	}

	private static void assertOwnerOnlyAcl(Path path) throws IOException {
		AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);

		assertThat(aclView.getAcl()).singleElement().satisfies(entry -> {
			assertThat(entry.type()).isEqualTo(AclEntryType.ALLOW);
			assertThat(entry.principal()).isEqualTo(aclView.getOwner());
			assertThat(entry.permissions())
					.isEqualTo(EnumSet.allOf(AclEntryPermission.class));
		});
	}

	@Test
	void rejectsSymbolicLinksAsPrivateWorkDirectories() throws Exception {
		Path actualDirectory = Files.createDirectory(temporaryDirectory.resolve("actual"));
		Path symbolicLink = temporaryDirectory.resolve("work");
		try {
			Files.createSymbolicLink(symbolicLink, actualDirectory);
		} catch (IOException | UnsupportedOperationException ex) {
			Assumptions.assumeTrue(false, "Symbolic links are not available: " + ex.getMessage());
		}

		assertThatIOException()
				.isThrownBy(() -> SecureFileUtils.ensurePrivateDirectory(symbolicLink))
				.withMessageContaining("not a regular directory");
	}
}
