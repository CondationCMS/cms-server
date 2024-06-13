package com.github.thmarx.cms.cli.commands.modules;

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
import com.github.thmarx.cms.CMSServer;
import com.github.thmarx.cms.cli.tools.ModulesUtil;
import com.github.thmarx.cms.extensions.repository.InstallationHelper;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "remove")
public class RemoveCommand extends AbstractModuleCommand implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<module>",
			index = "0",
			description = "The id of the module."
	)
	private String module = "";

	@Override
	public void run() {

		if (CMSServer.isRunning()) {
			System.out.println("modules can not be modified in running system");
			return;
		}

		if (!isInstalled(module)) {
			System.err.println("module is not installed");
			return;
		}
		
		if (ModulesUtil.getRequiredModules().contains(module)) {
			System.err.println("can not delete required module");
			return;
		}

		InstallationHelper.deleteDirectory(getModuleFolder(module).toFile());
		
		System.out.printf("module '%s' removed", module);
	}

}
