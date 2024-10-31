package com.condation.cms.core.theme;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.ThemeProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.messages.MessageSource;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedThemeProperties;
import com.condation.cms.core.messages.EmptyMessageSource;
import com.condation.cms.core.messages.ThemeMessageSource;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTheme implements Theme {

	public static final Theme EMPTY = new DefaultTheme(null, new EmptyThemeProperties(Map.of()), true, new EmptyMessageSource());

	private final Path themePath;
	private final ThemeProperties properties;
	private final MessageSource messages;
	private boolean empty = false;
	
	private Theme parent;

	private DefaultTheme(final Path themePath, final ThemeProperties themeProperties, final boolean empty, final MessageSource messages) {
		this(themePath, themeProperties, messages);
		this.empty = empty;
	}

	public static Theme load(
			Path themePath, 
			SiteProperties siteProperties, 
			MessageSource siteMessages, 
			ServerProperties serverProperties,
			CacheManager cacheManager) throws IOException {
		
		return load(themePath, siteProperties, siteMessages, serverProperties, cacheManager, true);
	}
	
	private static Theme load(
			Path themePath, 
			SiteProperties siteProperties, 
			MessageSource siteMessages, 
			ServerProperties serverProperties, CacheManager cacheManager, boolean withParent) throws IOException {
		
		ICache<String, String> cache = null;
		if (withParent) {
			cache = cacheManager.get("theme.messages", new CacheManager.CacheConfig(500l, Duration.ofMinutes(5)));
		} else {
			cache = cacheManager.get("theme.parent.messages", new CacheManager.CacheConfig(500l, Duration.ofMinutes(5)));
		}

		MessageSource messages = new ThemeMessageSource(siteProperties, themePath.resolve("messages/"), siteMessages, cache);
		
		var themeConfiguration = ConfigurationFactory.themeConfiguration("theme", themePath.getFileName().toString());
		
		final ThemeProperties themeProperties = new ExtendedThemeProperties(themeConfiguration);
		final DefaultTheme defaultTheme = new DefaultTheme(themePath, themeProperties, messages);
		if (withParent && themeProperties.parent() != null) {
			var parentTheme = DefaultTheme.load(
					serverProperties.getThemesFolder().resolve(themeProperties.parent()), 
					siteProperties, 
					messages,
					serverProperties,
					cacheManager,
					false
			);
			defaultTheme.parent = parentTheme;
		}

		return defaultTheme;
	}

	@Override
	public boolean empty() {
		return empty;
	}

	@Override
	public ThemeProperties properties() {
		return properties;
	}

	@Override
	public String getName() {
		return (String) properties.name();
	}

	@Override
	public Path assetsPath() {
		if (themePath == null) {
			return null;
		}
		return themePath.resolve(Constants.Folders.ASSETS);
	}

	@Override
	public Path templatesPath() {
		if (themePath == null) {
			return null;
		}
		return themePath.resolve(Constants.Folders.TEMPLATES);
	}

	@Override
	public Path extensionsPath() {
		if (themePath == null) {
			return null;
		}

		return themePath.resolve(Constants.Folders.EXTENSIONS);
	}

	@Override
	public MessageSource getMessages() {
		if (parent != null) {
			return parent.getMessages();
		}
		return messages;
	}

	@Override
	public Theme getParentTheme() {
		return parent;
	}

	
	public Path resolve(String path, Path override, Path parent) {
		var resolved = override.resolve(path);
		if (resolved != null) {
			return resolved;
		}
		if (parent == null) {
			return null;
		}
		return parent.resolve(path);
	}
	
	@Override
	public Path resolveExtension(String path) {
		return resolve(
				path, 
				extensionsPath(),
				getParentTheme() != null ? getParentTheme().extensionsPath() : null
		);
	}

	@Override
	public Path resolveAsset(String path) {
		return resolve(
				path, 
				assetsPath(),
				getParentTheme() != null ? getParentTheme().assetsPath() : null
		);
	}

	@Override
	public Path resolveTemplate(String path) {
		return resolve(
				path, 
				templatesPath(),
				getParentTheme() != null ? getParentTheme().templatesPath() : null
		);
	}
}
