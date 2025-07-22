package com.condation.cms.cli.commands.server;

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



import com.condation.cms.cli.tools.CLIServerUtils;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(
		name = "info",
		description = {
			"returns some information about the server"
		}
)
@Slf4j
public class Info implements Runnable {

	@Override
	public void run() {
		try {
			var info = new StringBuilder();
			
			info.append("Info about CMS-Server").append(System.lineSeparator());
			info.append("=====================").append(System.lineSeparator());
			info.append("Version: ").append(CLIServerUtils.getVersion().toString()).append(System.lineSeparator());
			info.append("Running: ").append(CLIServerUtils.getCMSProcess().isPresent()).append(System.lineSeparator());
			info.append("Started at: ").append(CLIServerUtils.getStartedAt()).append(System.lineSeparator());
			info.append("Uptime: ").append(CLIServerUtils.getUptime()).append(System.lineSeparator());
			
			System.out.println(info.toString());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
