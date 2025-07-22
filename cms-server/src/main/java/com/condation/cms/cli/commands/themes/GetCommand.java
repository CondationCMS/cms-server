package com.condation.cms.cli.commands.themes;

import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;

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


import com.condation.cms.extensions.repository.InstallationHelper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(
		name = "get",
		description = {
			"gets a theme from registry by id",
			"to force update of already installed theme use -f"
		}
)
public class GetCommand extends AbstractThemeCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<theme>",
			index = "0",
			description = "The id of the theme."
	)
	private String theme = "";

	@CommandLine.Option(names = "-f", description = "force the update if theme is already installed")
	boolean forceUpdate;

	@Override
	public void run() {

		if (Strings.isNullOrEmpty(theme)) {
			System.err.println("provide a theme name");
			return;
		}
		if (!getRepository().exists(theme)) {
			System.err.printf("Theme %s not found\r\n", theme);
			return;
		}

		if (!isCompatibleWithServer(theme)) {
			System.err.println("theme is not compatible with server version");
			return;
		}

		if (isInstalled(theme) && !forceUpdate) {
			System.err.println("theme is already installed, use -f to force an update");
			return;
		}

		if (isInstalled(theme)) {
			InstallationHelper.deleteDirectory(getThemeFolder(theme).toFile());
		}

		var info = getRepository().getInfo(theme).get();

		System.out.println("get theme");
		getRepository().download(info.getFile(), info.getSignature(), ServerUtil.getPath(Constants.Folders.THEMES));
		System.out.println("theme downloaded");
	}

}
