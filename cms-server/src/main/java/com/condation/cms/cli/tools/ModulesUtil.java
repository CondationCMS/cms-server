package com.condation.cms.cli.tools;

import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;

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
import com.condation.cms.core.utils.SiteUtil;
import com.condation.cms.cli.commands.modules.AbstractModuleCommand;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import com.condation.cms.core.configuration.properties.ExtendedThemeProperties;
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

			var serverConfig = ConfigurationFactory.serverConfiguration();
			ExtendedServerProperties serverProperties = new ExtendedServerProperties(serverConfig);
			requiredModules.addAll(serverProperties.activeModules());
			
			var hosts = ServerUtil.getPath(Constants.Folders.HOSTS);
			var themes = ServerUtil.getPath(Constants.Folders.THEMES);
			if (Files.exists(hosts)) {
				try (var hostStream = Files.list(hosts)) {
					hostStream.filter(ModulesUtil::isHost)
							.forEach(site -> {
								try {
									var hostProperties = new ExtendedSiteProperties(ConfigurationFactory.siteConfiguration("bla", site));
									requiredModules.addAll(hostProperties.activeModules());
								} catch (IOException ex) {
									log.error("", ex);
								}
							});
				}
			}
			if (Files.exists(themes)) {
				try (var themesStream = Files.list(themes)) {
					themesStream
						.filter(ModulesUtil::isTheme)
						.forEach(themeConfig -> {
							try {
								var themeProperties = new ExtendedThemeProperties(ConfigurationFactory.themeConfiguration("theme", themeConfig.getFileName().toString()));
								requiredModules.addAll(themeProperties.activeModules());
							} catch (IOException ex) {
								log.error("", ex);
							}
						});
				}
			}
		} catch (IOException ex) {
			log.error("", ex);
		}

		return requiredModules;
	}

	public static boolean isHost(Path host) {
		return SiteUtil.isSite(host);
	}

	public static boolean isTheme(Path host) {
		return Files.exists(host.resolve("theme.yaml"))
				|| Files.exists(host.resolve("theme.toml"));
	}
}
