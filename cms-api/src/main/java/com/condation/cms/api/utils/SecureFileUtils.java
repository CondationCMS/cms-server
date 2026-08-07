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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Creates private work files below an application-owned directory instead of
 * the shared operating-system temp directory.
 */
public final class SecureFileUtils {

	private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
			PosixFilePermissions.fromString("rwx------");
	private static final Set<PosixFilePermission> FILE_PERMISSIONS =
			PosixFilePermissions.fromString("rw-------");
	private static final Set<AclEntryPermission> OWNER_PERMISSIONS =
			Set.copyOf(EnumSet.allOf(AclEntryPermission.class));

	private SecureFileUtils() {
	}

	public static Path ensurePrivateDirectory(Path directory) throws IOException {
		Path normalized = directory.toAbsolutePath().normalize();
		Path parent = normalized.getParent();
		if (parent == null) {
			throw new IOException("Private directory must have a parent: " + directory);
		}
		Files.createDirectories(parent);

		try {
			if (supportsPosix(parent)) {
				Files.createDirectory(
						normalized,
						PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS));
			} else {
				Files.createDirectory(normalized);
			}
		} catch (FileAlreadyExistsException _) {
			// Validate and tighten an existing application work directory below.
		}

		if (Files.isSymbolicLink(normalized)
				|| !Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException("Private work path is not a regular directory: " + normalized);
		}
		restrictToOwner(normalized);
		return normalized.toRealPath();
	}

	public static Path createPrivateTempDirectory(Path privateParent, String prefix) throws IOException {
		Path parent = ensurePrivateDirectory(privateParent);
		if (supportsPosix(parent)) {
			return Files.createTempDirectory(
					parent,
					prefix,
					PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS));
		}

		Path directory = Files.createTempDirectory(parent, prefix);
		restrictToOwner(directory);
		return directory;
	}

	public static Path createPrivateTempFile(Path privateParent, String prefix, String suffix)
			throws IOException {
		Path parent = ensurePrivateDirectory(privateParent);
		if (supportsPosix(parent)) {
			return Files.createTempFile(
					parent,
					prefix,
					suffix,
					PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
		}

		Path file = Files.createTempFile(parent, prefix, suffix);
		restrictToOwner(file);
		return file;
	}

	private static boolean supportsPosix(Path path) throws IOException {
		return Files.getFileStore(path).supportsFileAttributeView("posix");
	}

	private static void restrictToOwner(Path path) throws IOException {
		if (supportsPosix(path)) {
			Files.setPosixFilePermissions(
					path,
					Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
							? DIRECTORY_PERMISSIONS
							: FILE_PERMISSIONS);
			return;
		}

		AclFileAttributeView aclView = Files.getFileAttributeView(
				path,
				AclFileAttributeView.class,
				LinkOption.NOFOLLOW_LINKS);
		if (aclView == null) {
			throw new IOException(
					"File system does not support POSIX permissions or ACLs: " + path);
		}

		AclEntry ownerAccess = AclEntry.newBuilder()
				.setType(AclEntryType.ALLOW)
				.setPrincipal(aclView.getOwner())
				.setPermissions(OWNER_PERMISSIONS)
				.build();
		aclView.setAcl(List.of(ownerAccess));
	}
}
