package com.condation.cms.filesystem;

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

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.filesystem.metadata.query.ExtendableQuery;

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
public class PresistentFileSystemTest {

	static FileSystem fileSystem;
	
	
	@BeforeAll
	static void setup() throws IOException {
		
		var eventBus = Mockito.mock(EventBus.class);
		
		fileSystem = new FileSystem(Path.of("src/test/resources"), eventBus, (file) -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init(MetaData.Type.PERSISTENT);
	}
	
	@AfterAll
	static void shutdown () throws IOException {
		fileSystem.shutdown();
		
		if (Files.exists(Path.of("src/test/resources/data"))) {
			FileUtils.deleteFolder(Path.of("src/test/resources/data"));
		}
	}

	@Test
	public void test_query() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).where("featured", true).get();
		Assertions.assertThat(nodes).hasSize(2);
	}
	
	@Test
	public void test_query_in() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).whereIn("name", "test1", "test2").get();
		Assertions.assertThat(nodes).hasSize(2);
	}
	
	@Test
	public void test_query_not_in() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).whereNotIn("name", "test1", "test2").get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).data().get("name")).isEqualTo("start");
	}
	@Test
	public void test_query_contains() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).whereContains("taxonomy.tags", "eins").get();
		Assertions.assertThat(nodes).hasSize(1);
	}
	
	@Test
	public void test_query_contains_not() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).whereNotContains("taxonomy.tags", "eins").get();
		Assertions.assertThat(nodes).hasSize(1);
	}
	
	@Test
	public void test_lt_lte() throws IOException {
		var nodes = fileSystem.query((node, i) -> node).where("number2", "<", 5).get();
		Assertions.assertThat(nodes).hasSize(0);
		
		nodes = fileSystem.query((node, i) -> node).where("number2", "lte", 5).get();
		Assertions.assertThat(nodes).hasSize(1);
	}

	@Test
	public void test_query_with_start_uri() throws IOException {

		var nodes = fileSystem.query("/test", (node, i) -> node).where("featured", true).get();
		
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("test/test1.md");
	}
	
	@Test
	public void test_field_exists() throws IOException {

		var nodes = fileSystem.query("/test", (node, i) -> node).whereExists("keywords").get();
		
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("test/test1.md");
		
		nodes = fileSystem.query((node, i) -> node).whereExists("keywords").get();
		
		Assertions.assertThat(nodes).hasSize(2);
	}
	
	@Test
	public void test_custom_operation() throws IOException {

		var query = fileSystem.query((node, i) -> node);
		var nodes = query.get();
		Assertions.assertThat(nodes).hasSize(3);
		
		query = fileSystem.query((node, i) -> node);
		((ExtendableQuery)query).addCustomOperators("none", (value1, value2) -> false);
		nodes = query.where("featured", "none").get();
		Assertions.assertThat(nodes).hasSize(0);
	}

	@Test
	public void test_sorting() throws IOException {

		var nodes = fileSystem.query((node, i) -> node)
			.orderby("publish_date").asc()
			.get();
		
		Assertions.assertThat(nodes).hasSize(3);
		Assertions.assertThat(nodes.get(0).data().get("name")).isEqualTo("start");
		Assertions.assertThat(nodes.get(1).data().get("name")).isEqualTo("test1");
		Assertions.assertThat(nodes.get(2).data().get("name")).isEqualTo("test2");
		
		nodes = fileSystem.query((node, i) -> node)
			.orderby("publish_date").desc()
			.get();
		
		Assertions.assertThat(nodes).hasSize(3);
		Assertions.assertThat(nodes.get(0).data().get("name")).isEqualTo("test2");
		Assertions.assertThat(nodes.get(1).data().get("name")).isEqualTo("test1");
		Assertions.assertThat(nodes.get(2).data().get("name")).isEqualTo("start");
	}

	@Test
	public void test_sorting_page() throws IOException {

		var page = fileSystem.query((node, i) -> node)
			.orderby("publish_date").asc()
			.page(1, 10);
		
		Assertions.assertThat(page.getItems()).hasSize(3);
		Assertions.assertThat(page.getItems().get(0).data().get("name")).isEqualTo("start");
		Assertions.assertThat(page.getItems().get(1).data().get("name")).isEqualTo("test1");
		Assertions.assertThat(page.getItems().get(2).data().get("name")).isEqualTo("test2");
		
		page = fileSystem.query((node, i) -> node)
			.orderby("publish_date").desc()
			.page(1, 10);
		
		Assertions.assertThat(page.getItems()).hasSize(3);
		Assertions.assertThat(page.getItems().get(0).data().get("name")).isEqualTo("test2");
		Assertions.assertThat(page.getItems().get(1).data().get("name")).isEqualTo("test1");
		Assertions.assertThat(page.getItems().get(2).data().get("name")).isEqualTo("start");
	}
}
