package com.github.thmarx.cms.api.configuration.loader;

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


import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.configuration.Loader;
import com.github.thmarx.cms.api.configuration.configs.SiteConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class SiteConfigurationLoader implements Loader<SiteConfiguration> {

	private final Path hostBase;
	private final String env;
	
	private void mergeEnvConfig (final SiteProperties siteProperties) throws IOException {
		var envFile = hostBase.resolve("site-%s.yaml".formatted(env));
		if (Files.exists(envFile)) {
			var configs = PropertiesLoader.rawProperties(envFile);
			siteProperties.merge(configs);
		}
	}
	
	@Override
	public SiteConfiguration load() throws IOException {
		var props = hostBase.resolve("site.yaml");
		final SiteProperties siteProperties = PropertiesLoader.hostProperties(props);
		
		mergeEnvConfig(siteProperties);
		
		return new SiteConfiguration(siteProperties);
	}

	@Override
	public void reload(final SiteConfiguration config) throws IOException {
		var props = hostBase.resolve("site.yaml");
		config.siteProperties().update(PropertiesLoader.rawProperties(props));
		
		mergeEnvConfig(config.siteProperties());
	}
	
}
