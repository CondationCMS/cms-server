package com.condation.cms.core.configuration;

/*-
 * #%L
 * tests
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
		configuration.add(
				ServerConfiguration.class, 
				new ServerConfiguration(new ExtendedServerProperties((SimpleConfiguration) get("server").get()))
		);
		
		configuration.add(
				SiteConfiguration.class, 
				new SiteConfiguration(new ExtendedSiteProperties((SimpleConfiguration) get("site").get()))
		);
		configuration.add(
				com.condation.cms.api.configuration.configs.TaxonomyConfiguration.class, 
				new com.condation.cms.api.configuration.configs.TaxonomyConfiguration(
						((com.condation.cms.core.configuration.configs.TaxonomyConfiguration) get("taxonomy")
								.get()).getTaxonomies()
				)
		);
		configuration.add(
				com.condation.cms.api.configuration.configs.MediaConfiguration.class, 
				new com.condation.cms.api.configuration.configs.MediaConfiguration(
						((com.condation.cms.core.configuration.configs.MediaConfiguration) get("media")
								.get()).getMediaFormats()
				)
		);
		
	}
}
