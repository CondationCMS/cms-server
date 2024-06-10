package com.github.thmarx.cms.cli;

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
import com.github.thmarx.cms.cli.commands.AddUser;
import com.github.thmarx.cms.cli.commands.ExtensionCommands;
import com.github.thmarx.cms.cli.commands.RemoveUser;
import com.github.thmarx.cms.cli.commands.Startup;
import com.github.thmarx.cms.cli.commands.Stop;
import com.github.thmarx.cms.cli.commands.ModuleCommands;
import com.github.thmarx.cms.cli.commands.ReloadHost;
import com.github.thmarx.cms.cli.commands.ThemeCommands;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "server", subcommands = {
	Startup.class, AddUser.class, RemoveUser.class, Stop.class, ReloadHost.class, ExtensionCommands.class, ModuleCommands.class, ThemeCommands.class})
@Slf4j
public class ServerCommand implements Runnable {

	@Override
	public void run() {
		System.out.println("server command");
	}
}
