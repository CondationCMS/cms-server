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
import com.condation.cms.api.Constants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CLIServerUtilsTest {

	@TempDir
	Path serverHome;

	private String previousServerHome;

	@BeforeEach
	void configureServerHome() {
		previousServerHome = System.getProperty("cms.home");
		System.setProperty("cms.home", serverHome.toString());
	}

	@AfterEach
	void restoreServerHome() {
		if (previousServerHome == null) {
			System.clearProperty("cms.home");
		} else {
			System.setProperty("cms.home", previousServerHome);
		}
	}

	@Test
	void writtenPidFileIdentifiesCurrentProcess() throws Exception {
		CLIServerUtils.writePidFile();

		Assertions.assertThat(CLIServerUtils.getCMSProcess())
				.contains(ProcessHandle.current());

		var properties = readPidFile();
		Assertions.assertThat(properties.getProperty("pid"))
				.isEqualTo(Long.toString(ProcessHandle.current().pid()));
		Assertions.assertThat(properties.getProperty("startedAt"))
				.isEqualTo(Long.toString(currentProcessStart()));
	}

	@Test
	void reusedPidIsNotRecognizedAsCmsProcess() throws Exception {
		writePidFile(ProcessHandle.current().pid(), currentProcessStart() - 1);

		Assertions.assertThat(CLIServerUtils.getCMSProcess()).isEmpty();
		Assertions.assertThat(pidFile()).doesNotExist();
	}

	@Test
	void legacyPidFileIsRemovedBecauseItCannotBeVerified() throws Exception {
		Files.writeString(pidFile(), Long.toString(ProcessHandle.current().pid()));

		Assertions.assertThat(CLIServerUtils.getCMSProcess()).isEmpty();
		Assertions.assertThat(pidFile()).doesNotExist();
	}

	private long currentProcessStart() {
		return ProcessHandle.current().info().startInstant()
				.map(Instant::toEpochMilli)
				.orElseThrow();
	}

	private void writePidFile(long pid, long startedAt) throws Exception {
		Files.writeString(pidFile(), "pid=" + pid + System.lineSeparator()
				+ "startedAt=" + startedAt + System.lineSeparator());
	}

	private Properties readPidFile() throws Exception {
		var properties = new Properties();
		try (var reader = Files.newBufferedReader(pidFile())) {
			properties.load(reader);
		}
		return properties;
	}

	private Path pidFile() {
		return serverHome.resolve(Constants.PID_FILE);
	}
}
