package com.condation.cms.filesystem.taxonomy;

/*-
 * #%L
 * cms-filesystem
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
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.filesystem.FileSystem;
import com.condation.cms.filesystem.MetaData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class PersistentFileTaxonomiesTest {

	static FileSystem fileSystem;

	static FileTaxonomies taxonomies;

	@BeforeAll
	public static void setup() throws IOException {
		var config = new Configuration();
		var tags = new Taxonomy("Tags", "tags", "taxonomy.tags");
		tags.setArray(true);
		config.add(TaxonomyConfiguration.class, new TaxonomyConfiguration(new ConcurrentHashMap<>(
				Map.of(
						"kategorien", new Taxonomy("Kategorie", "kategorien", "taxonomy.category"),
						"tags", tags
				)
		)));

		var eventBus = Mockito.mock(EventBus.class);

		fileSystem = new FileSystem("test-site", Path.of("src/test/resources"), eventBus, (file) -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init(MetaData.Type.PERSISTENT);

		taxonomies = new FileTaxonomies(config, fileSystem);
	}

	@AfterAll
	public static void close() throws IOException {
		fileSystem.shutdown();

		if (Files.exists(Path.of("src/test/resources/data"))) {
			FileUtils.deleteFolder(Path.of("src/test/resources/data"));
		}
	}

	@Test
	public void test_slug() throws IOException {
		Assertions.assertThat(taxonomies.forSlug("tags")).isPresent();
		Assertions.assertThat(taxonomies.forSlug("author")).isEmpty();
	}

	@Test
	public void test_values() throws IOException {
		var tags = taxonomies.forSlug("tags").get();
		var values = taxonomies.values(tags);

		Assertions.assertThat(values).containsExactlyInAnyOrder("eins", "zwei", "drei");

//		Assertions.assertThat(tags.getValues()).containsOnlyKeys("eins", "zwei");
//		Assertions.assertThat(tags.getValues().get("eins").title).isEqualTo("Eins");
//		Assertions.assertThat(tags.getValues().get("zwei").title).isEqualTo("Zwei");
	}

	@Test
	public void test_with_value() throws IOException {
		var tags = taxonomies.forSlug("tags").get();
		taxonomies.withValue(tags, "eins");

		taxonomies.withValue(tags, "drei");
	}

}
