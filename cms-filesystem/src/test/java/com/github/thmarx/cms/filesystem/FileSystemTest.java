package com.github.thmarx.cms.filesystem;

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
import com.github.thmarx.cms.api.db.ContentNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public class FileSystemTest {

	static FileSystem fileSystem;

	@BeforeAll
	static void setup() throws IOException {
		fileSystem = new FileSystem(Path.of("src/test/resources"), null, (file) -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init();
	}

	@Test
	public void test_dimension() throws IOException {

		var dimension = fileSystem.createDimension("featured", (ContentNode node) -> node.data().containsKey("featured") ? (Boolean) node.data().get("featured") : false, Boolean.class);

		Assertions.assertThat(dimension.filter(Boolean.TRUE)).hasSize(2);
		Assertions.assertThat(dimension.filter(Boolean.FALSE)).hasSize(1);
	}

	@Test
	public void test_query() throws IOException {

		var nodes = fileSystem.query((node, i) -> node).where("featured", true).get();
		
		Assertions.assertThat(nodes).hasSize(2);
	}

	@Test
	public void test_query_with_start_uri() throws IOException {

		var nodes = fileSystem.query("/test", (node, i) -> node).where("featured", true).get();
		
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("test/test1.md");
	}
}
