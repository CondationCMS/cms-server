package com.github.thmarx.cms.filesystem.taxonomy;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public class FileTaxonomiesTest {
	
	static FileSystem fileSystem;

	static FileTaxonomies taxonomies;
	
	@BeforeAll
	public static void setup () throws IOException {
		var siteProps = PropertiesLoader.hostProperties(Path.of("src/test/resources/site.yaml"));
		
		var config = new Configuration(Path.of("src/test/resources/"));
		
		fileSystem = new FileSystem(Path.of("src/test/resources"), null, (file) -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init();
		
		taxonomies = new FileTaxonomies(config, fileSystem);
	}
	
	@AfterAll
	public static void close () {
		fileSystem.shutdown();
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
		
		Assertions.assertThat(tags.getValues()).containsOnlyKeys("eins", "zwei");
		Assertions.assertThat(tags.getValues().get("eins").title).isEqualTo("Eins");
		Assertions.assertThat(tags.getValues().get("zwei").title).isEqualTo("Zwei");
	}
	
	@Test
	public void test_with_value() throws IOException {
		var tags = taxonomies.forSlug("tags").get();
		taxonomies.withValue(tags, "eins");
		
		taxonomies.withValue(tags, "drei");
	}
	
}
