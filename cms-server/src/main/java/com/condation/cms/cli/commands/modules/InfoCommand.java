package com.condation.cms.cli.commands.modules;

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


import com.condation.cms.CMSServer;
import lombok.Setter;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(
		name = "info",
		description = {
			"displays some information about a module"
		}
)
public class InfoCommand extends AbstractModuleCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<module>",
			index = "0",
			description = "The id of the module."
	)
	@Setter
	private String module = "";

	@Override
	public void run() {
		if (!getRepository().exists(module)) {
			System.err.printf("module %s not found", module);
			return;
		}
		var info = getRepository().getInfo(module).get();

		System.out.println("module: " + info.getId());
		System.out.println("name: " + info.getName());
		System.out.println("description: " + info.getDescription());
		System.out.println("author: " + info.getAuthor());
		System.out.println("url: " + info.getUrl());
		System.out.println("file: " + info.getFile());
		System.out.println("compatibility: " + info.getCompatibility());
		System.out.println("your server version: " + CMSServer.getVersion().getVersion());
		System.out.println("compatibility with server version: " + CMSServer.getVersion().satisfies(info.getCompatibility()));
		
		System.out.println("local installed: " + isInstalled(info.getId()));
		
		if (isInstalled(info.getId())) {
			var versionOpt = getLocaleModuleVersion(module);
			if (versionOpt.isPresent()) {
				System.out.println("installed version: " + versionOpt.get());
			} else {
				System.out.println("installed version: ERROR");
			}
		}
	}
}
