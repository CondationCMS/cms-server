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
import com.condation.cms.extensions.repository.InstallationHelper;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "get")
public class GetCommand extends AbstractModuleCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<module>",
			index = "0",
			description = "The id of the module."
	)
	private String module = "";

	@CommandLine.Option(names = "-f", description = "force the update if module is already installed")
	boolean forceUpdate;

	@Override
	public void run() {

		if (CMSServer.isRunning()) {
			System.out.println("modules can not be modified in running system");
			return;
		}

		if (isInstalled(module) && !forceUpdate) {
			System.err.println("module is already installed, use -f to force an update");
			return;
		}

		if (isInstalled(module)) {
			InstallationHelper.deleteDirectory(getModuleFolder(module).toFile());
		}

		if (getRepository().exists(module)) {

			if (!isCompatibleWithServer(module)) {
				System.err.println("module is not compatible with server version");
				return;
			}

			var info = getRepository().getInfo(module).get();

			System.out.printf("get module %s \r\n", module);
			getRepository().download(info.getFile(), Path.of("modules/"));
			System.out.println("module downloaded");
		} else {
			System.out.printf("can not find module %s in registry", module);
		}
	}

}
