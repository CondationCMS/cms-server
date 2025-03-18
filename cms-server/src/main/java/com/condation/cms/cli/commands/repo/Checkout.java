package com.condation.cms.cli.commands.repo;

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



import com.condation.cms.api.Constants;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.ipc.Command;
import com.condation.cms.ipc.IPCClient;
import com.google.common.base.Strings;
import java.nio.file.Files;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "checkout")
@Slf4j
public class Checkout implements Runnable {

	@CommandLine.Parameters(
			paramLabel = "<repo>",
			index = "0",
			description = "The name of the repository in the git.yaml."
	)
	private String repo = "";
	
	@Override
	public void run() {
		try {
			
			Optional<ProcessHandle> handle = getCMSProcess();
			
			if (Strings.isNullOrEmpty(repo)) {
				System.err.println("no repository specified");
				System.exit(1);
			}
			
			if (handle.isEmpty()) {
				System.out.println("server not running");
			} else {
				ServerProperties properties = new ExtendedServerProperties(ConfigurationFactory.serverConfiguration());
				
				IPCClient ipcClient = new IPCClient(properties.ipc());
				
				ipcClient.send(new Command("repo_checkout").setHeader("repo", repo));
			}
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Optional<ProcessHandle> getCMSProcess () throws Exception {
		var pidFile = ServerUtil.getPath(Constants.PID_FILE);
		if (!Files.exists(pidFile)) {
			return Optional.empty();
		}
		var pid = Files.readString(pidFile);
		return ProcessHandle.of(Long.parseLong(pid.trim()));
	}

	
}
