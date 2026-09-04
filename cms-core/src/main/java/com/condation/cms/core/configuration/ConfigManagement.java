package com.condation.cms.core.configuration;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class ConfigManagement {
	
	private final Map<String, IConfiguration> configurations = new HashMap<>();

	public boolean has (String key) {
		return configurations.containsKey(key);
	}
	
	public <T extends IConfiguration> void add (String key, T configuration) {
		configurations.put(key, configuration);
	}
	
	public <T extends IConfiguration> Optional<T> get (String key) {
		return Optional.ofNullable((T)configurations.get(key));
	}
	
	public void reload () {
		configurations.values().forEach(IConfiguration::reload);
	}
	
	public void initConfiguration (Configuration configuration) {
		var serverConfiguration = require("server", SimpleConfiguration.class);
		var siteConfiguration = require("site", SimpleConfiguration.class);
		var taxonomyConfiguration = require(
				"taxonomy", com.condation.cms.core.configuration.configs.TaxonomyConfiguration.class);
		var collectionConfiguration = require(
				"collections", com.condation.cms.core.configuration.configs.CollectionConfiguration.class);
		var mediaConfiguration = require(
				"media", com.condation.cms.core.configuration.configs.MediaConfiguration.class);

		configuration.add(
				ServerConfiguration.class, 
				new ServerConfiguration(new ExtendedServerProperties(serverConfiguration))
		);
		
		configuration.add(
				SiteConfiguration.class, 
				new SiteConfiguration(new ExtendedSiteProperties(siteConfiguration))
		);
		configuration.add(
				com.condation.cms.api.configuration.configs.TaxonomyConfiguration.class, 
				new com.condation.cms.api.configuration.configs.TaxonomyConfiguration(
						taxonomyConfiguration.getTaxonomies()
				)
		);
		configuration.add(
				com.condation.cms.api.configuration.configs.CollectionConfiguration.class,
				collectionConfiguration.apiConfiguration()
		);
		var mediaConfig = new com.condation.cms.api.configuration.configs.MediaConfiguration(
						mediaConfiguration.getMediaFormats()
				);
		mediaConfig.setProcessor(mediaConfiguration.getProcessor());
		mediaConfig.setBinPath(mediaConfiguration.getValueOrDefault("bin_path", ""));
		configuration.add(
				com.condation.cms.api.configuration.configs.MediaConfiguration.class, 
				mediaConfig
		);
		
	}

	private <T extends IConfiguration> T require(String key, Class<T> type) {
		var configuration = configurations.get(key);
		if (configuration == null) {
			throw new IllegalStateException("Missing configuration: " + key);
		}
		if (!type.isInstance(configuration)) {
			throw new IllegalStateException("Configuration '%s' is not of type %s"
					.formatted(key, type.getSimpleName()));
		}
		return type.cast(configuration);
	}
}
