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


import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.TaxonomyConfiguration;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public class ConfigurationReloadTest {
	
	static FileSystem fileSystem;
	
	static Configuration configuration;
	
	@BeforeAll
	static void setup() throws IOException {
		
		Files.deleteIfExists(Path.of("reload/config/taxonomy.yaml"));
		Files.deleteIfExists(Path.of("reload/config/taxonomy.tags.yaml"));
		
		var eventBus = Mockito.mock(EventBus.class);
		
		fileSystem = new FileSystem(Path.of("reload/"), eventBus, (file) -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init();
		
		configuration = new Configuration(fileSystem.hostBase());
	}
	
	@AfterAll
	static void shutdown () {
		fileSystem.shutdown();
	}
	
	@Test
	void test_taxonomy () throws IOException {
		
		TaxonomyConfiguration config = configuration.get(TaxonomyConfiguration.class);
		
		Assertions.assertThat(config.getTaxonomies()).isEmpty();
		
		Files.copy(
				Path.of("reload/taxonomies/taxonomy.yaml"), 
				Path.of("reload/config/taxonomy.yaml")
		);
		Files.copy(
				Path.of("reload/taxonomies/taxonomy.tags.yaml"), 
				Path.of("reload/config/taxonomy.tags.yaml")
		);
		
		configuration.reload(TaxonomyConfiguration.class);
		
		Assertions.assertThat(config.getTaxonomies()).isNotEmpty();
	}
}
