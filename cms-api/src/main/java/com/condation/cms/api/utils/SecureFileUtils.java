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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
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
		restrictToOwner(normalized, true);
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
		restrictToOwner(directory, true);
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
		restrictToOwner(file, false);
		return file;
	}

	private static boolean supportsPosix(Path path) throws IOException {
		return Files.getFileStore(path).supportsFileAttributeView("posix");
	}

	private static void restrictToOwner(Path path, boolean directory) throws IOException {
		if (supportsPosix(path)) {
			Files.setPosixFilePermissions(
					path,
					directory ? DIRECTORY_PERMISSIONS : FILE_PERMISSIONS);
			return;
		}

		File file = path.toFile();
		boolean restricted = file.setReadable(false, false)
				&& file.setWritable(false, false)
				&& (!directory || file.setExecutable(false, false))
				&& file.setReadable(true, true)
				&& file.setWritable(true, true)
				&& (!directory || file.setExecutable(true, true));
		if (!restricted) {
			throw new IOException("Could not restrict work path permissions: " + path);
		}
	}
}
