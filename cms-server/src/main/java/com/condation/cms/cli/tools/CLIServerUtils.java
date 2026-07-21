package com.condation.cms.cli.tools;

/*-
 * #%L
 * CMS Server
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
import com.condation.cms.Startup;
import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import org.semver4j.Semver;

/**
 *
 * @author thorstenmarx
 */
public class CLIServerUtils {

	private static final String PID_PROPERTY = "pid";
	private static final String STARTED_AT_PROPERTY = "startedAt";
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

	public static Optional<ProcessHandle> getCMSProcess() throws IOException {
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		if (!Files.exists(pidFile)) {
			return Optional.empty();
		}

		var properties = new Properties();
		try (var reader = Files.newBufferedReader(pidFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}

		final long pid;
		final long expectedStart;
		try {
			pid = Long.parseLong(properties.getProperty(PID_PROPERTY));
			expectedStart = Long.parseLong(properties.getProperty(STARTED_AT_PROPERTY));
		} catch (NumberFormatException | NullPointerException ex) {
			return removeStalePidFile(pidFile);
		}

		var process = ProcessHandle.of(pid);
		if (process.isEmpty() || !process.get().isAlive()) {
			return removeStalePidFile(pidFile);
		}

		var actualStart = process.get().info().startInstant().map(Instant::toEpochMilli);
		if (actualStart.isEmpty() || actualStart.get() != expectedStart) {
			return removeStalePidFile(pidFile);
		}

		return process;
	}

	private static Optional<Instant> getStartTime() {
		try {
			var process = getCMSProcess();
			if (process.isPresent()) {
				return process.get().info().startInstant();
			}
		} catch (Exception ex) {
			System.getLogger(CLIServerUtils.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
		}
		return Optional.empty();
	}

	public static String getStartedAt() {
		var startTime = getStartTime();
		if (startTime.isEmpty()) {
			return "";
		}
		var started = startTime.get();
		return formatter.format(started);
	}

	public static String getUptime() {
		var startTime = getStartTime();
		if (startTime.isEmpty()) {
			return "";
		}

		Instant now = Instant.now();
		Duration uptime = Duration.between(startTime.get(), now);

		long hours = uptime.toHours();
		long minutes = uptime.toMinutes() % 60; // Rest-Minuten nach Stunden

		return String.format("%dh %dm", hours, minutes);
	}

	public static void writePidFile() throws IOException {
		var process = ProcessHandle.current();
		var startedAt = process.info().startInstant()
				.orElseThrow(() -> new IOException("process start time is not available"));

		var content = PID_PROPERTY + "=" + process.pid() + System.lineSeparator()
				+ STARTED_AT_PROPERTY + "=" + startedAt.toEpochMilli() + System.lineSeparator();
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		var temporaryPidFile = pidFile.resolveSibling(pidFile.getFileName() + ".tmp");

		Files.writeString(temporaryPidFile, content, StandardCharsets.UTF_8);
		try {
			Files.move(temporaryPidFile, pidFile,
					StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException ex) {
			Files.move(temporaryPidFile, pidFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static Optional<ProcessHandle> removeStalePidFile(Path pidFile) throws IOException {
		Files.deleteIfExists(pidFile);
		return Optional.empty();
	}

	public static Semver getVersion() {
		try (var in = Startup.class.getResourceAsStream("application.properties")) {
			Properties props = new Properties();
			props.load(in);

			return Semver.coerce(props.getProperty("version"));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
