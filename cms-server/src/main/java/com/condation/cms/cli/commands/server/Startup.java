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
import com.condation.cms.api.ServerContext;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.cli.tools.ModulesUtil;
import com.condation.cms.cli.tools.ThemesUtil;
import com.condation.cms.git.RepositoryManager;
import com.condation.cms.ipc.IPCServer;
import com.condation.cms.server.configs.ServerGlobalModule;
import com.condation.cms.server.JettyServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "start")
@Slf4j
public class Startup implements Runnable {

	@Override
	public void run() {
		try {
			
			System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
			System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");

			var globalInjector = Guice.createInjector(new ServerGlobalModule());
			ServerProperties properties = globalInjector.getInstance(ServerProperties.class);
			
			checkInstalledModules();
			checkInstalledThemes();
			
			printStartup(properties);

			ServerContext.IS_DEV = properties.dev();

			initGitRepositoryManager(globalInjector);
			
			var server = new JettyServer(globalInjector);
			
			var ipcServer = new IPCServer(properties.ipc(), server::fireServerEvent);
			ipcServer.start();

			server.startup();
			writePidFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void checkInstalledThemes () {
		var requiredThemes = ThemesUtil.getRequiredThemes();
		log.trace("check required themes: " + requiredThemes);
		if (!ThemesUtil.allInstalled(requiredThemes)) {
			var toInstall = ThemesUtil.filterUnInstalled(requiredThemes);
			log.error("following themes are missing");
			toInstall.forEach(log::error);
			log.error("install via: java cms-server.jar theme get <module_id>");
			System.exit(1);
		} else {
			log.trace("all required themes are intalled");
		}
	}
	
	private void checkInstalledModules() {
		var modules = ModulesUtil.getRequiredModules();
		log.trace("check required modules: " + modules);
		if (!ModulesUtil.allInstalled(modules)) {
			var toInstall = ModulesUtil.filterUnInstalled(modules);
			log.error("following modules are missing");
			toInstall.forEach(log::error);
			log.error("install via: java cms-server.jar module get <module_id>");
			System.exit(1);
		} else {
			log.trace("all required modules are intalled");
		}
	}
	
	
	private static void writePidFile () throws IOException {
		Files.deleteIfExists(Path.of(Constants.PID_FILE));
		Files.writeString(Path.of(Constants.PID_FILE), String.valueOf(ProcessHandle.current().pid()));
	}

	private static void initGitRepositoryManager(Injector globaInjector) throws IOException {
		Path gitConfig = Path.of("git.yaml");
		if (!Files.exists(gitConfig)) {
			log.info("no repository configuration found");
			return;
		}
		log.info("repository configuration found");
		final RepositoryManager repositoryManager = new RepositoryManager(globaInjector.getInstance(Scheduler.class));
		repositoryManager.init(gitConfig);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				repositoryManager.close();
			} catch (IOException ex) {
				log.error("error closing repo manager", ex);
			}
		}));
	}

	private static void printStartup(ServerProperties properties) throws IOException {
		try (var in = com.condation.cms.Startup.class.getResourceAsStream("application.properties")) {
			Properties props = new Properties();
			props.load(in);

			log.info("starting {} version {}", props.getProperty("name"), props.getProperty("version"));
			log.info("build {}", props.getProperty("build.date"));
			log.info("environment {}", properties.env());
		}
	}
}
