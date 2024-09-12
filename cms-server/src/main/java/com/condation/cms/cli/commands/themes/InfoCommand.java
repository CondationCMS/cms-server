package com.condation.cms.cli.commands.themes;

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
import com.google.common.base.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "info")
public class InfoCommand extends AbstractThemeCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<module>",
			index = "0",
			description = "The id of the module."
	)
	@Setter
	private String theme = "";

	@Override
	public void run() {
		if (Strings.isNullOrEmpty(theme)) {
			System.err.println("provide a theme name");
			return;
		}
		System.out.println("theme: " + theme);
		if (getRepository().exists(theme)) {
			var info = getRepository().getInfo(theme).get();

			System.out.println("name: " + info.getName());
			System.out.println("description: " + info.getDescription());
			System.out.println("author: " + info.getAuthor());
			System.out.println("url: " + info.getUrl());
			System.out.println("file: " + info.getFile());
			System.out.println("compatibility: " + info.getCompatibility());
			System.out.println("your server version: " + CMSServer.getVersion().getVersion());
			System.out.println("compatibility with server version: " + CMSServer.getVersion().satisfies(info.getCompatibility()));
			System.out.println("local installed: " + isInstalled(info.getId()));
		} else {
			System.out.println("repository: not found");
		}
		
		if (isInstalled(theme)) {
			var versionOpt = getLocaleThemeVersion(theme);
			if (versionOpt.isPresent()) {
				System.out.println("installed version: " + versionOpt.get());
			} else {
				System.out.println("installed version: ERROR");
			}
		} else {
			System.out.println("locale: not found");
		}
	}
	
	

}
