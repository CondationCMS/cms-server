package com.github.thmarx.cms.cli.tools;

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


import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.cli.commands.modules.AbstractModuleCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ModulesUtil {

	public static boolean allInstalled(Set<String> modules) {
		return modules.stream().allMatch(AbstractModuleCommand::isInstalled);
	}

	public static Set<String> filterUnInstalled(Set<String> modules) {
		return modules.stream().filter(module -> !AbstractModuleCommand.isInstalled(module)).collect(Collectors.toSet());
	}

	public static Set<String> getRequiredModules() {
		Set<String> requiredModules = new HashSet<>();
		try {

			var hosts = Path.of("hosts/");
			var themes = Path.of("themes/");
			if (Files.exists(hosts)) {
				Files.list(hosts)
						.filter(ModulesUtil::isHost)
						.map(host -> host.resolve("site.yaml"))
						.forEach(site -> {
							try {
								var hostProperties = PropertiesLoader.hostProperties(site);
								requiredModules.addAll(hostProperties.activeModules());
							} catch (IOException ex) {
								log.error("", ex);
							}
						});
			}
			if (Files.exists(themes)) {
				Files.list(themes)
						.filter(ModulesUtil::isTheme)
						.map(host -> host.resolve("theme.yaml"))
						.forEach(site -> {
							try {
								var hostProperties = PropertiesLoader.themeProperties(site);
								requiredModules.addAll(hostProperties.activeModules());
							} catch (IOException ex) {
								log.error("", ex);
							}
						});
			}
		} catch (IOException ex) {
			log.error("", ex);
		}

		return requiredModules;
	}

	public static boolean isHost(Path host) {
		return Files.exists(host.resolve("site.yaml"));
	}

	public static boolean isTheme(Path host) {
		return Files.exists(host.resolve("theme.yaml"));
	}
}
