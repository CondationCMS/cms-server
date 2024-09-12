package com.condation.cms.api.configuration;

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


import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.configuration.configs.TaxonomyConfiguration;
import com.condation.cms.api.configuration.loader.SiteConfigurationLoader;
import com.condation.cms.api.configuration.loader.TaxonomyConfigurationLoader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class Configuration {

	private final Path hostBase;

	private Map<Class<? extends Config>, Config> configs = new HashMap<>();

	public <T extends Config> void add(Class<T> configClass, T config) {
		configs.put(configClass, config);
	}

	public <T extends Config> T get(Class<T> configClass) {
		if (!configs.containsKey(configClass)) {
			loadConfig(configClass);
		}
		return (T) configs.get(configClass);
	}

	public void reload(Class<? extends Config> configClass) {
		try {
			log.debug("reload config + " + configClass.getSimpleName());
			if (configClass.equals(SiteConfiguration.class)) {
				var env = get(ServerConfiguration.class).serverProperties().env();
				new SiteConfigurationLoader(hostBase, env).reload((SiteConfiguration)configs.get(configClass));
			} else if (configClass.equals(TaxonomyConfiguration.class)) {
				new TaxonomyConfigurationLoader(hostBase).reload((TaxonomyConfiguration)configs.get(configClass));
			}
			
		} catch (IOException e) {
			log.error(null, e);
			throw new RuntimeException(e);
		}
	}
	
	private void loadConfig(Class<? extends Config> configClass) {
		try {
			if (configClass.equals(SiteConfiguration.class)) {
				var env = get(ServerConfiguration.class).serverProperties().env();
				configs.put(configClass, new SiteConfigurationLoader(hostBase, env).load());
			} else if (configClass.equals(TaxonomyConfiguration.class)) {
				configs.put(configClass, new TaxonomyConfigurationLoader(hostBase).load());
			}
		} catch (IOException e) {
			log.error(null, e);
			throw new RuntimeException(e);
		}
	}

}
