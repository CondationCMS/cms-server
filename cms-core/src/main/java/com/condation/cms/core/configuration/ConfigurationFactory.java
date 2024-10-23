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

	public static ConfigManagement create(Path hostBase, EventBus eventBus, CronJobScheduler cronScheduler) throws IOException {
		ConfigManagement management = new ConfigManagement();

		final SimpleConfiguration serverConfiguration = serverConfiguration(eventBus);
		final SimpleConfiguration siteConfiguration = siteConfiguration(
				eventBus, 
				serverConfiguration.getString("env", "dev"), 
				hostBase,
				new CronReload("0/10 * * * * ?", cronScheduler)
		);
		final TaxonomyConfiguration taxonomyConfiguration = taxonomyConfiguration(
				eventBus, 
				hostBase,
				new CronReload("0/10 * * * * ?", cronScheduler)
		);

		final SimpleConfiguration themeConfiguration = themeConfiguration(
				"theme", 
				eventBus, 
				siteConfiguration.getString("theme", "null")
		);
		final SimpleConfiguration parentThemeConfiguration = themeConfiguration(
				"parent-theme", 
				eventBus, 
				themeConfiguration.getString("parent", "null")
		);
		
		final MediaConfiguration mediaConfiguration = mediaConfiguration(
				eventBus, 
				hostBase,
				List.of(
						siteConfiguration.getString("theme", "null"),
						themeConfiguration.getString("parent", "null")
				)
		);
		
		management.add(serverConfiguration.id(), serverConfiguration);
		management.add(siteConfiguration.id(), siteConfiguration);
		management.add(taxonomyConfiguration.id(), taxonomyConfiguration);
		management.add(mediaConfiguration.id(), mediaConfiguration);
		management.add(themeConfiguration.id(), themeConfiguration);
		management.add(themeConfiguration.id(), parentThemeConfiguration);
		
		return management;
	}

	public static SimpleConfiguration themeConfiguration(String id, String themePath) throws IOException {
		return themeConfiguration(id, null, themePath);
	}
	private static SimpleConfiguration themeConfiguration(String id, EventBus eventBus, String theme) throws IOException {
		return SimpleConfiguration.builder(eventBus)
				.id(id)
				.reloadStrategy(new NoReload())
				.addSource(YamlConfigSource.build(Path.of("themes/%s/theme.yaml".formatted(theme))))
				.addSource(TomlConfigSource.build(Path.of("themes/%s/theme.toml".formatted(theme))))
				.build();
	}

	public static SimpleConfiguration serverConfiguration() throws IOException {
		return serverConfiguration(null);
	}
	private static SimpleConfiguration serverConfiguration(EventBus eventBus) throws IOException {
		return SimpleConfiguration.builder(eventBus)
				.id("server")
				.reloadStrategy(new NoReload())
				.addSource(YamlConfigSource.build(Path.of("server.yaml")))
				.addSource(TomlConfigSource.build(Path.of("server.toml")))
				.build();
	}

	private static MediaConfiguration mediaConfiguration(EventBus eventBus, Path hostBase, List<String> themes) throws IOException {
		List<ConfigSource> themeSources = new ArrayList<>();
		for (String theme : themes) {
			themeSources.add(
					YamlConfigSource.build(Path.of("themes/%s/config/media.yaml".formatted(theme))));
			themeSources.add(
					TomlConfigSource.build(Path.of("themes/%s/config/media.toml".formatted(theme))));
		};
		
		themeSources.add(YamlConfigSource.build(hostBase.resolve("config/media.yaml")));
		themeSources.add(TomlConfigSource.build(hostBase.resolve("config/media.toml")));
		
		return MediaConfiguration.builder(eventBus)
				.id("media")
				.reloadStrategy(new NoReload())
				.addAllSources(themeSources)
				.build();
	}

	public static SimpleConfiguration siteConfiguration(String env, Path hostBase) throws IOException {
		return siteConfiguration(null, env, hostBase, new NoReload());
	}
	
	private static SimpleConfiguration siteConfiguration(EventBus eventBus, String env, Path siteBase, ReloadStrategy reloadStrategy) throws IOException {

		List<ConfigSource> siteSources = new ArrayList<>();
		siteSources.add(YamlConfigSource.build(siteBase.resolve("site.yaml")));
		siteSources.add(TomlConfigSource.build(siteBase.resolve("site.toml")));

		var envFile = siteBase.resolve("site-%s.yaml".formatted(env));
		if (Files.exists(envFile)) {
			siteSources.add(YamlConfigSource.build(envFile));
		}
		envFile = siteBase.resolve("site-%s.toml".formatted(env));
		if (Files.exists(envFile)) {
			siteSources.add(TomlConfigSource.build(envFile));
		}

		var config = SimpleConfiguration.builder(eventBus)
				.id("site")
				.reloadStrategy(reloadStrategy);

		siteSources.forEach(config::addSource);

		return config.build();
	}

	private static TaxonomyConfiguration taxonomyConfiguration(EventBus eventBus, Path hostBase, ReloadStrategy reloadStrategy) throws IOException {

		return TaxonomyConfiguration.builder(eventBus)
				.id("taxonomy")
				.reloadStrategy(reloadStrategy)
				.addSource(YamlConfigSource.build(hostBase.resolve("config/taxonomy.yaml")))
				.addSource(TomlConfigSource.build(hostBase.resolve("config/taxonomy.toml")))
				.build();
	}
}
