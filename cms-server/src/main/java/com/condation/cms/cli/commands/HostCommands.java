package com.condation.cms.cli.commands;

/*-
 * #%L
 * CMS Server
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import com.condation.cms.cli.commands.host.ReloadHost;
import com.condation.cms.cli.commands.host.ReIndexHost;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(
		name = "host",
		description = {
			"Host/site related commands"
		},
		mixinStandardHelpOptions = true,
		subcommands = {
			ReloadHost.class,
			ReIndexHost.class
		})
public class HostCommands implements Runnable {

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec commandSpec;

	@Override
	public void run() {
		commandSpec.commandLine().getOut().println("Subcommand needed: 'reload' or 'reindex'");
	}
}
