package com.github.thmarx.cms.cli.commands.extensions;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.CMSServer;
import com.github.thmarx.cms.extensions.repository.Repository;
import org.semver4j.Semver;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "info")
public class InfoCommand implements Runnable {

	Repository repository = new Repository();
	
	@CommandLine.Parameters(
			paramLabel = "<extension>",
			index = "0",
			description = "The id of the extension."
	)
	private String extension = "";
	
	@Override
	public void run() {
		System.out.println("ext info command");
		System.out.println("module: " + repository.exists(extension));
		if (repository.exists(extension)) {
			var info = repository.getInfo(extension);
			var compatibility = (String)info.get("compatibility");
			System.out.println("server: " +  CMSServer.getVersion().getVersion());
			System.out.println("compatibility: " +  compatibility);
			System.out.println("compatible with server version: " + CMSServer.getVersion().satisfies(compatibility));
		}
	}
}
