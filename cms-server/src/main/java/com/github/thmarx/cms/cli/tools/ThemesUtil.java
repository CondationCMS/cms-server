package com.github.thmarx.cms.cli.tools;

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

import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.cli.commands.modules.AbstractModuleCommand;
import com.github.thmarx.cms.cli.commands.themes.AbstractThemeCommand;
import com.google.common.base.Strings;
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
public class ThemesUtil {

	public static boolean allInstalled (Set<String> themes) {
		return themes.stream().allMatch(AbstractThemeCommand::isInstalled);
	}
	
	public static Set<String> filterUnInstalled (Set<String> themes) {
		return themes.stream().filter(theme -> !AbstractThemeCommand.isInstalled(theme)).collect(Collectors.toSet());
	}
	
	public static Set<String> getRequiredThemes () {
		Set<String> requiredThemes = new HashSet<>();
		try {
			Files.list(Path.of("hosts/"))
					.filter(ThemesUtil::isHost)
					.map(host -> host.resolve("site.yaml"))
					.forEach(site -> {
						try {
							var hostProperties = PropertiesLoader.hostProperties(site);
							if (!Strings.isNullOrEmpty(hostProperties.theme())) {
								requiredThemes.add(hostProperties.theme());
							}
						} catch (IOException ex) {
							log.error("", ex);
						}
					});
		} catch (IOException ex) {
			log.error("", ex);
		}
		
		return requiredThemes;
	}
	
	public static boolean isHost(Path host) {
		return Files.exists(host.resolve("site.yaml"));
	}
}
