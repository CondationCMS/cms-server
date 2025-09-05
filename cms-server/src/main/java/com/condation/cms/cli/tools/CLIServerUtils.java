package com.condation.cms.cli.tools;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.Startup;
import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;
import java.io.IOException;
import java.nio.file.Files;
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

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

	public static Optional<ProcessHandle> getCMSProcess() throws Exception {
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		if (!Files.exists(pidFile)) {
			return Optional.empty();
		}
		var pid = Files.readString(pidFile);
		return ProcessHandle.of(Long.parseLong(pid.trim()));
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
		Files.deleteIfExists(ServerUtil.getPath(Constants.PID_FILE));
		Files.writeString(ServerUtil.getPath(Constants.PID_FILE), String.valueOf(ProcessHandle.current().pid()));
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
