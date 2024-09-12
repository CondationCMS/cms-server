package com.github.thmarx.cms.api.configuration;

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

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.configuration.configs.ServerConfiguration;
import com.github.thmarx.cms.api.configuration.configs.SiteConfiguration;
import com.github.thmarx.cms.api.configuration.configs.TaxonomyConfiguration;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.ConfigurationFileChanged;
import com.github.thmarx.cms.api.scheduler.CronJobContext;
import com.github.thmarx.cms.api.scheduler.CronJobScheduler;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
public class ConfigurationManagement {

	final DB db;
	@Getter
	final Configuration configuration;
	final CronJobScheduler scheduler;
	final EventBus eventBus;

	private final List<ConfigurationResource> watched_configurations = new ArrayList<>();

	public void reload () throws IOException {
		watched_configurations.clear();
		init_files();
	}
	
	private void init_files () throws IOException {
		// init config files
		addPathToWatch(db.getFileSystem().resolve("site.yaml"), SiteConfiguration.class);
		
		var env = configuration.get(ServerConfiguration.class).serverProperties().env();
		var envFile = db.getFileSystem().resolve("site-%s.yaml".formatted(env));
		if (Files.exists(envFile)) {
			addPathToWatch(envFile, SiteConfiguration.class);
		}
		
		var taxoPath = db.getFileSystem().resolve("config/taxonomy.yaml");
		addPathToWatch(taxoPath, TaxonomyConfiguration.class);
		if (Files.exists(taxoPath.getParent())) {
			Files.list(taxoPath.getParent())
					.filter(path -> Constants.TAXONOMY_VALUE.matcher(path.getFileName().toString()).matches())
					.forEach(path -> {
						try {
							addPathToWatch(path, TaxonomyConfiguration.class);
						} catch (IOException ioe) {
							log.error(null, ioe);
						}
					});
		}
	}
	
	public void init() throws IOException {
		init("0 * * * * ?");
	}
	
	public void init(String cronExpression) throws IOException {
		init_files();

		// setup scheduler
		scheduler.schedule(cronExpression, "configuration-updater", this::update);
	}
	
	private void addPathToWatch(final Path configFile, final Class<? extends Config> configClass) throws IOException {
		if (!Files.exists(configFile)) {
			return;
		}
		watched_configurations.add(
				new ConfigurationResource(configFile, configClass, 0)
		);
	}

	private List<ConfigurationResource> getConfigurations () {
		return new ArrayList<>(watched_configurations);
	}
	
	
	public void update(CronJobContext jobContext) {
		System.out.println("update");
		log.trace("check for modified configurations {}", db.getFileSystem().resolve(".").toString());
		getConfigurations().forEach(config -> {
			try {
				var tempMod = Files.getLastModifiedTime(config.configFile).toMillis();

				if (tempMod != config.lastModified) {
					log.debug("modified: " + config.configFile.getFileName().toString());
					config.setLastModified(tempMod);

					eventBus.publish(new ConfigurationFileChanged(config.configClass));
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		});
	}

	@AllArgsConstructor
	@Data
	public static class ConfigurationResource {

		Path configFile;
		Class<? extends Config> configClass;
		long lastModified;
	}
}
