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
import com.github.thmarx.cms.cli.tools.ThemesUtil;
import com.github.thmarx.cms.extensions.repository.InstallationHelper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "remove")
public class RemoveCommand extends AbstractThemeCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<theme>",
			index = "0",
			description = "The id of the theme."
	)
	private String theme = "";

	@Override
	public void run() {

		if (Strings.isNullOrEmpty(theme)) {
			System.err.println("provide a theme name");
			return;
		}
		
		if (!isInstalled(theme)) {
			System.err.println("Theme '%s' is not installed");
			return;
		}
		
		if (ThemesUtil.getRequiredThemes().contains(theme)) {
			System.err.println("can not delete required theme");
			return;
		}

		InstallationHelper.deleteDirectory(getThemeFolder(theme).toFile());
		System.out.printf("theme '%s' removed", theme);

		
	}

}
