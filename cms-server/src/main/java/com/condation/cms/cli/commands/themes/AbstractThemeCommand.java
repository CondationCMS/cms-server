package com.condation.cms.cli.commands.themes;

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
import com.condation.cms.api.PropertiesLoader;
import com.condation.cms.extensions.repository.ModuleInfo;
import com.condation.cms.extensions.repository.RemoteModuleRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public abstract class AbstractThemeCommand {

	public static final String DEFAULT_REGISTRY_URL = "https://raw.githubusercontent.com/thmarx/theme-registry";

	@Getter
	private RemoteModuleRepository<ModuleInfo> repository = new RemoteModuleRepository(ModuleInfo.class, DEFAULT_REGISTRY_URL);

	protected static Path getThemeFolder (String theme) {
		return Path.of("themes/" + theme);
	}
	
	protected boolean isCompatibleWithServer(String extension) {
		var info = repository.getInfo(extension);
		if (info.isEmpty()) {
			throw new RuntimeException("theme not found");
		}

		return CMSServer.getVersion().satisfies(info.get().getCompatibility());
	}

	public static boolean isInstalled(String theme) {
		return Files.exists(getThemeFolder(theme));
	}

	protected Optional<Double> getLocaleThemeVersion(String theme) {
		try {
			var themePath = getThemeFolder(theme);
			if (!Files.exists(themePath)) {
				return Optional.empty();
			}
			var themeProperties = PropertiesLoader.themeProperties(themePath.resolve("theme.yaml"));
			return Optional.ofNullable(themeProperties.version());
		} catch (IOException ex) {
			log.error("", ex);
		}
		return Optional.empty();
	}
}
