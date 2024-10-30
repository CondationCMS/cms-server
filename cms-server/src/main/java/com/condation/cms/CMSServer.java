package com.condation.cms;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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



import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.semver4j.Semver;

/**
 *
 * @author t.marx
 */
@Slf4j
public class CMSServer {

	public static boolean isRunning () {
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		if (!Files.exists(pidFile)) {
			return false;
		}
		try {
			var pid = Files.readString(pidFile);
			
			return ProcessHandle.of(Long.parseLong(pid)).isPresent();
		} catch (IOException ex) {
			log.error("", ex);
		}
		return false;
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
