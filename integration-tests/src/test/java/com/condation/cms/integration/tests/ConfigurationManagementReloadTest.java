package com.condation.cms.integration.tests;

/*-
 * #%L
 * integration-tests
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

import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.ConfigurationManagement;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.core.scheduler.SingleCronJobScheduler;
import com.condation.cms.filesystem.FileDB;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author t.marx
 */
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
public class ConfigurationManagementReloadTest {

	static FileDB db;

	static Configuration configuration;

	static ConfigurationManagement configurationManagement;

	static Scheduler scheduler;

	static MockEventBus eventBus = new MockEventBus();

	@BeforeAll
	static void setup() throws IOException, SchedulerException {

		var serverProps = Mockito.mock(ServerProperties.class);
		
		Mockito.when(serverProps.env()).thenReturn("dev");
		
		configuration = new Configuration(Path.of("reload/"));
		configuration.add(ServerConfiguration.class, new ServerConfiguration(serverProps));

		db = new FileDB(Path.of("reload/"), eventBus, (path) -> Map.of(), configuration);
		db.init();

		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		scheduler = schedulerFactory.getScheduler();
		scheduler.start();

		
		configurationManagement = new ConfigurationManagement(db, configuration, new SingleCronJobScheduler(scheduler, new CronJobContext()), eventBus);
		configurationManagement.init("0/5 * * * * ?");
	}
	
	@BeforeEach
	void prepare_clean_run () throws IOException {
		Files.deleteIfExists(Path.of("reload/config/taxonomy.yaml"));
		Files.deleteIfExists(Path.of("reload/config/taxonomy.tags.yaml"));
	}

	@AfterAll
	static void shutdown() throws Exception {
		db.close();
		scheduler.shutdown();
	}

	@Test
	void test_taxonomy() throws Exception {

		eventBus.reset();
		Files.copy(
				Path.of("reload/taxonomies/taxonomy.yaml"),
				Path.of("reload/config/taxonomy.yaml")
		);
		Files.copy(
				Path.of("reload/taxonomies/taxonomy.tags.yaml"),
				Path.of("reload/config/taxonomy.tags.yaml")
		);

		configurationManagement.reload();
		
		Thread.sleep(Duration.ofSeconds(6));
		
		Assertions.assertThat(eventBus.pubCounter).isEqualTo(2);
	}

	static class MockEventBus implements EventBus {

		private int pubCounter = 0;

		public void reset() {
			pubCounter = 0;
		}

		@Override
		public <T extends Event> void syncPublish(T event) {
		}

		@Override
		public <T extends Event> void publish(T event) {
			pubCounter++;
		}

		@Override
		public <T extends Event> void register(Class<T> eventClass, EventListener<T> listener) {
		}

		@Override
		public <T extends Event> void unregister(Class<T> eventClass, EventListener<T> listener) {
		}

		@Override
		public void unregister(EventListener listener) {
		}
	}
}
