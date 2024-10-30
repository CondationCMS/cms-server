package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.theme.Theme;

/**
 *
 * @author thmar
 */
public class SiteUtil {

	private SiteUtil() {
	}

	public static boolean isSite(Path check) {
		return Files.exists(check.resolve("site.yaml"))
				|| Files.exists(check.resolve("site.toml"));
	}

	public static List<String> getActiveModules(SiteProperties siteProperties, Theme theme) {
		List<String> activeModules = new ArrayList<>();
		activeModules.addAll(siteProperties.activeModules());
		if (!theme.empty()) {
			activeModules.addAll(theme.properties().activeModules());
			
			if (theme.getParentTheme() != null) {
				activeModules.addAll(theme.getParentTheme().properties().activeModules());
			}
		}
		return activeModules;
	}
	
	public static String getRequiredTheme(SiteProperties siteProperties, Theme theme) {
		return siteProperties.theme();
	}
}
