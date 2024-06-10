package com.github.thmarx.cms.cli.commands.themes;

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

import com.github.thmarx.cms.extensions.repository.InstallationHelper;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "get")
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
		
		if (getRepository().exists(theme)) {
			
			if (!isCompatibleWithServer(theme)) {
				throw new RuntimeException("theme is not compatible with server version");
			}
			
			if (isInstalled(theme) && !forceUpdate) {
				throw new RuntimeException("theme is already installed, use -f to force an update");
			}

			if (isInstalled(theme)) {
				InstallationHelper.deleteDirectory(getThemeFolder(theme).toFile());
			}
			
			var info = getRepository().getInfo(theme).get();
			
			System.out.println("get theme");
			System.out.println("from: " + info.getFile());
			getRepository().download(info.getFile(), Path.of("themes/"));
		}
	}
	
}
