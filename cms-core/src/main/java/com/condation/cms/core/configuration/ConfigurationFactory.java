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

import com.condation.cms.api.db.DB;
import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.scheduler.CronJobScheduler;
import com.condation.cms.core.configuration.configs.MediaConfiguration;
import com.condation.cms.core.configuration.configs.TaxonomyConfiguration;
import com.condation.cms.core.configuration.reload.CronReload;
import com.condation.cms.core.configuration.reload.NoReload;
import com.condation.cms.core.configuration.source.TomlConfigSource;
import com.condation.cms.core.configuration.source.YamlConfigSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class ConfigurationFactory {

	public static ConfigManagement create (DB db, EventBus eventBus, CronJobScheduler cronScheduler) throws IOException {
		ConfigManagement management = new ConfigManagement();
		
		final SimpleConfiguration serverConfiguration = serverConfiguration(eventBus);
		final SimpleConfiguration siteConfiguration = siteConfiguration(eventBus, serverConfiguration.getString("env", "dev"), db, cronScheduler);
		final TaxonomyConfiguration taxonomyConfiguration = taxonomyConfiguration(eventBus, db);
		final MediaConfiguration mediaConfiguration = mediaConfiguration(eventBus, db);
		
		management.add(serverConfiguration.id(), serverConfiguration);
		management.add(siteConfiguration.id(), siteConfiguration);
		management.add(taxonomyConfiguration.id(), taxonomyConfiguration);
		management.add(mediaConfiguration.id(), mediaConfiguration);
		
		return management;
	}
	
	public static SimpleConfiguration serverConfiguration (EventBus eventBus) throws IOException {
		return SimpleConfiguration.builder(eventBus)
				.id("server")
				.reloadStrategy(new NoReload())
				.addSource(YamlConfigSource.build(Path.of("server.yaml")))
				.addSource(TomlConfigSource.build(Path.of("server.toml")))
				.build();
	}
	
	private static MediaConfiguration mediaConfiguration (EventBus eventBus, DB db) throws IOException {
		return MediaConfiguration.builder(eventBus)
				.id("media")
				.reloadStrategy(new NoReload())
				.addSource(YamlConfigSource.build(db.getFileSystem().resolve("config/media.yaml")))
				.addSource(TomlConfigSource.build(db.getFileSystem().resolve("config/media.toml")))
				.build();
	}
	
	private static SimpleConfiguration siteConfiguration (EventBus eventBus, String env, DB db, CronJobScheduler cronScheduler) throws IOException {
		
		List<ConfigSource> siteSources = new ArrayList<>();
		siteSources.add(YamlConfigSource.build(db.getFileSystem().resolve("site.yaml")));
		siteSources.add(TomlConfigSource.build(db.getFileSystem().resolve("site.toml")));
		
		var envFile = db.getFileSystem().resolve("site-%s.yaml".formatted(env));
		if (Files.exists(envFile)) {
			siteSources.add(YamlConfigSource.build(envFile));
		}
		envFile = db.getFileSystem().resolve("site-%s.toml".formatted(env));
		if (Files.exists(envFile)) {
			siteSources.add(TomlConfigSource.build(envFile));
		}
		
		var config = SimpleConfiguration.builder(eventBus)
				.id("site")
				.reloadStrategy(new CronReload("0/10 * * * * ?", cronScheduler));
		
		siteSources.forEach(config::addSource);
		
		return config.build();
	}
	
	private static TaxonomyConfiguration taxonomyConfiguration (EventBus eventBus, DB db) throws IOException {
		
		return TaxonomyConfiguration.builder(eventBus)
				.id("taxonomy")
				.addSource(YamlConfigSource.build(db.getFileSystem().resolve("config/taxonomy.yaml")))
				.addSource(TomlConfigSource.build(db.getFileSystem().resolve("config/taxonomy.toml")))
				.build();
	}
}
