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
import java.util.Optional;
import java.util.Properties;
import org.semver4j.Semver;

/**
 *
 * @author thorstenmarx
 */
public class CLIServerUtils {

	public static Optional<ProcessHandle> getCMSProcess() throws Exception {
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		if (!Files.exists(pidFile)) {
			return Optional.empty();
		}
		var pid = Files.readString(pidFile);
		return ProcessHandle.of(Long.parseLong(pid.trim()));
	}
	
	public static void writePidFile () throws IOException {
		Files.deleteIfExists(ServerUtil.getPath(Constants.PID_FILE));
		Files.writeString(ServerUtil.getPath(Constants.PID_FILE), String.valueOf(ProcessHandle.current().pid()));
	}
	
	public static Semver getVersion () {
		try (var in = Startup.class.getResourceAsStream("application.properties")) {
			Properties props = new Properties();
			props.load(in);
			
			return Semver.coerce(props.getProperty("version"));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
