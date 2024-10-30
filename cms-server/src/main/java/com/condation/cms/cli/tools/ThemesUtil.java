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



import com.condation.cms.api.utils.SiteUtil;
import com.condation.cms.cli.commands.themes.AbstractThemeCommand;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import com.condation.cms.core.configuration.properties.ExtendedThemeProperties;
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
		var themes = getRequiredSiteThemes();
		
		themes.addAll(getRequiredParentThemes());
		
		return themes;
	}
	
	public static Set<String> getRequiredSiteThemes () {
		Set<String> requiredThemes = new HashSet<>();
		try {
			Files.list(ServerUtil.getPath(Constants.Folders.HOSTS))
					.filter(ThemesUtil::isHost)
					.forEach(site -> {
						try {
							var hostProperties = new ExtendedSiteProperties(
									ConfigurationFactory.siteConfiguration("bla", site)
							);
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
	
	private static Set<String> getRequiredParentThemes () {
		Set<String> requiredThemes = new HashSet<>();
		try {
			Files.list(ServerUtil.getPath(Constants.Folders.THEMES))
					.filter(ThemesUtil::isTheme)
					.forEach(themeConfig -> {
						try {
							var themeProperties = new ExtendedThemeProperties(ConfigurationFactory.themeConfiguration("theme", themeConfig.getFileName().toString()));
							if (!Strings.isNullOrEmpty(themeProperties.parent())) {
								requiredThemes.add(themeProperties.parent());
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
	
	public static boolean isTheme(Path host) {
		return Files.exists(host.resolve("theme.yaml"));
	}
	
	public static boolean isHost(Path host) {
		return SiteUtil.isSite(host);
	}
}
