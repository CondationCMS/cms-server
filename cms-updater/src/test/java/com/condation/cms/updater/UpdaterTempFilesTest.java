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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class UpdaterTempFilesTest {
    private static final String USER_HOME = "user.home";

    @TempDir
    Path temporary;

    @Test
    void createsPrivateTemporaryFilesUnderUserHome() throws Exception {
        withUserHome(temporary, () -> {
            Path directory = UpdaterTempFiles.createDirectory("staging-");
            Path file = UpdaterTempFiles.createFile("download-", ".zip");

            Path expectedParent = temporary.resolve(".condationcms-updater-tmp");
            assertEquals(expectedParent, directory.getParent());
            assertEquals(expectedParent, file.getParent());
            assertTrue(Files.isDirectory(directory));
            assertTrue(Files.isRegularFile(file));
            assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE), Files.getPosixFilePermissions(directory));
            assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                    Files.getPosixFilePermissions(file));
        });
    }

    @Test
    void refusesUserHomeWritableByOtherUsers() throws Exception {
        Set<PosixFilePermission> originalPermissions;
        try {
            originalPermissions = Files.getPosixFilePermissions(temporary);
        } catch (UnsupportedOperationException _) {
            assumeTrue(false, "POSIX permissions are required for this test");
            return;
        }
        try {
            Files.setPosixFilePermissions(temporary, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OTHERS_WRITE));
            withUserHome(temporary, () -> assertThrows(IOException.class,
                    () -> UpdaterTempFiles.createFile("download-", ".zip")));
        } catch (UnsupportedOperationException _) {
            assumeTrue(false, "POSIX permissions are required for this test");
        } finally {
            Files.setPosixFilePermissions(temporary, originalPermissions);
        }
    }

    private static void withUserHome(Path home, ThrowingRunnable action) throws Exception {
        String previous = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, home.toString());
        try {
            action.run();
        } finally {
            if (previous == null) System.clearProperty(USER_HOME);
            else System.setProperty(USER_HOME, previous);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
