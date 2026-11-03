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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import java.util.Optional;

/** Reads the same PID and process-start metadata written by the CMS CLI. */
public final class ServerProcessChecker {
    public static final String PID_FILE_NAME = "cms.pid";
    private static final String PID_PROPERTY = "pid";
    private static final String STARTED_AT_PROPERTY = "startedAt";

    public Optional<ProcessHandle> getRunningProcess(Path installationDirectory) throws IOException {
        Path pidFile = installationDirectory.toAbsolutePath().normalize().resolve(PID_FILE_NAME);
        if (!Files.exists(pidFile)) return Optional.empty();

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(pidFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        final long pid;
        final long expectedStart;
        try {
            pid = Long.parseLong(properties.getProperty(PID_PROPERTY));
            expectedStart = Long.parseLong(properties.getProperty(STARTED_AT_PROPERTY));
        } catch (NumberFormatException | NullPointerException invalidPidFile) {
            Files.deleteIfExists(pidFile);
            return Optional.empty();
        }

        Optional<ProcessHandle> process = ProcessHandle.of(pid);
        if (process.isEmpty() || !process.get().isAlive()) {
            Files.deleteIfExists(pidFile);
            return Optional.empty();
        }
        Optional<Instant> actualStart = process.get().info().startInstant();
        if (actualStart.isEmpty() || actualStart.get().toEpochMilli() != expectedStart) {
            Files.deleteIfExists(pidFile);
            return Optional.empty();
        }
        return process;
    }

    public void ensureStopped(Path installationDirectory) throws IOException {
        Optional<ProcessHandle> process = getRunningProcess(installationDirectory);
        if (process.isPresent()) {
            throw new IOException("CondationCMS server is running (pid " + process.get().pid() + "); stop it before updating");
        }
    }
}
