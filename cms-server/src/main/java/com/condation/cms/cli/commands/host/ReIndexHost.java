package com.condation.cms.cli.commands.host;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ServerProperties;
import com.condation.cms.cli.tools.CLIServerUtils;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.ipc.Command;
import com.condation.cms.ipc.IPCClient;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
		name = "reindex",
		description = "Rebuilds all metadata indexes of a site. The server must be running."
)
public class ReIndexHost implements Callable<Integer> {

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec commandSpec;

	@CommandLine.Parameters(
			paramLabel = "<site>",
			index = "0",
			description = "The site to reindex."
	)
	private String site;

	@Override
	public Integer call() throws Exception {
		if (CLIServerUtils.getCMSProcess().isEmpty()) {
			commandSpec.commandLine().getErr().println("server not running");
			return 1;
		}

		ServerProperties properties = new ExtendedServerProperties(
				ConfigurationFactory.serverConfiguration());
		var ipcClient = new IPCClient(properties.ipc());
		ipcClient.send(new Command("reindex_host").setHeader("host", site));
		commandSpec.commandLine().getOut().printf("reindex of site '%s' triggered%n", site);
		return 0;
	}
}
