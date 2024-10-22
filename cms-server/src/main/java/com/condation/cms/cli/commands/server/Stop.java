package com.condation.cms.cli.commands.server;

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
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.ipc.Command;
import com.condation.cms.ipc.IPCClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "stop")
@Slf4j
public class Stop implements Runnable {

	@Override
	public void run() {
		try {
			
			Optional<ProcessHandle> handle = getCMSProcess();
			
			if (handle.isEmpty()) {
				System.out.println("can not find cms process");
			} else {
				ServerProperties properties = new ExtendedServerProperties(ConfigurationFactory.serverConfiguration());
				IPCClient ipcClient = new IPCClient(properties.ipc());
				
				ipcClient.send(new Command("shutdown"));
				
				Files.deleteIfExists(Path.of(Constants.PID_FILE));
			}
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Optional<ProcessHandle> getCMSProcess () throws Exception {
		if (!Files.exists(Path.of(Constants.PID_FILE))) {
			return Optional.empty();
		}
		var pid = Files.readString(Path.of(Constants.PID_FILE));
		return ProcessHandle.of(Long.parseLong(pid.trim()));
	}

	
}
