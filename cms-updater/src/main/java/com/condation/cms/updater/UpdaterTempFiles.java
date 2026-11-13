package com.condation.cms.updater;

/*-
 * #%L
 * CondationCMS Updater
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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

/** Creates updater temporary files in a private per-user directory. */
final class UpdaterTempFiles {
    private static final Set<PosixFilePermission> PRIVATE_DIRECTORY_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
    private static final Set<PosixFilePermission> PRIVATE_FILE_PERMISSIONS = Set.of(
            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

    private UpdaterTempFiles() { }

    static Path createDirectory(String prefix) throws IOException {
        Path parent = privateDirectory();
        try {
            return Files.createTempDirectory(parent, prefix, posixAttribute(PRIVATE_DIRECTORY_PERMISSIONS));
        } catch (UnsupportedOperationException _) {
            return Files.createTempDirectory(parent, prefix);
        }
    }

    static Path createFile(String prefix, String suffix) throws IOException {
        Path parent = privateDirectory();
        try {
            return Files.createTempFile(parent, prefix, suffix, posixAttribute(PRIVATE_FILE_PERMISSIONS));
        } catch (UnsupportedOperationException _) {
            return Files.createTempFile(parent, prefix, suffix);
        }
    }

    private static Path privateDirectory() throws IOException {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) throw new IOException("User home is not available for updater temporary files");

        Path home = Path.of(userHome).toAbsolutePath().normalize();
        if (!Files.isDirectory(home)) throw new IOException("User home is not a directory: " + home);
        try {
            Set<PosixFilePermission> homePermissions = Files.getPosixFilePermissions(home);
            if (homePermissions.contains(PosixFilePermission.GROUP_WRITE)
                    || homePermissions.contains(PosixFilePermission.OTHERS_WRITE)) {
                throw new IOException("User home is writable by other users: " + home);
            }
        } catch (UnsupportedOperationException _) {
            // Non-POSIX filesystems use the home directory's ACLs.
        }
        Path directory = home.resolve(".condationcms-updater-tmp");
        try {
            Files.createDirectory(directory, posixAttribute(PRIVATE_DIRECTORY_PERMISSIONS));
        } catch (FileAlreadyExistsException _) {
            // Validate the existing directory before using it.
        } catch (UnsupportedOperationException _) {
            try {
                Files.createDirectory(directory);
            } catch (FileAlreadyExistsException _) {
                // Validate the existing directory before using it.
            }
        }

        if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Updater temporary path is not a private directory: " + directory);
        }
        UserPrincipal homeOwner = Files.getOwner(home);
        UserPrincipal directoryOwner = Files.getOwner(directory, LinkOption.NOFOLLOW_LINKS);
        if (!homeOwner.equals(directoryOwner)) throw new IOException("Updater temporary directory has a different owner: " + directory);

        try {
            Files.setPosixFilePermissions(directory, PRIVATE_DIRECTORY_PERMISSIONS);
            if (!Files.getPosixFilePermissions(directory).equals(PRIVATE_DIRECTORY_PERMISSIONS)) {
                throw new IOException("Updater temporary directory permissions are not private: " + directory);
            }
        } catch (UnsupportedOperationException _) {
            // Non-POSIX filesystems use the user's home ACLs.
        }
        return directory;
    }

    private static FileAttribute<Set<PosixFilePermission>> posixAttribute(Set<PosixFilePermission> permissions) {
        return PosixFilePermissions.asFileAttribute(permissions);
    }
}
