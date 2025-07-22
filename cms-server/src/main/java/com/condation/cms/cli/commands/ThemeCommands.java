package com.condation.cms.cli.commands;

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


import com.condation.cms.cli.commands.themes.GetAllCommand;
import com.condation.cms.cli.commands.themes.GetCommand;
import com.condation.cms.cli.commands.themes.InfoCommand;
import com.condation.cms.cli.commands.themes.RemoveCommand;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(
		name = "theme",
		mixinStandardHelpOptions = true,
		description = {
			"commands to manage themes"
		},
		subcommands = {
			InfoCommand.class,
			GetCommand.class,
			GetAllCommand.class,
			RemoveCommand.class
		})
@Slf4j
public class ThemeCommands implements Runnable {

	@Override
	public void run() {
		System.out.println("Subcommand needed: 'get', 'get-all', 'remove' or 'info'");
	}
}
