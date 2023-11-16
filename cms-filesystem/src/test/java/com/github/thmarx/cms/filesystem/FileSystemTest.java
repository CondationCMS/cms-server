package com.github.thmarx.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

		var dimension = fileSystem.createDimension("featured", (MetaData.MetaNode node) -> node.data().containsKey("featured") ? (Boolean) node.data().get("featured") : false, Boolean.class);

		Assertions.assertThat(dimension.filter(Boolean.TRUE)).hasSize(2);
		Assertions.assertThat(dimension.filter(Boolean.FALSE)).hasSize(1);
	}

	@Test
	public void test_query() throws IOException {

		var nodes = fileSystem.query(node -> node).where("featured").is(true).get();
		
		Assertions.assertThat(nodes).hasSize(2);
	}

	@Test
	public void test_query_with_start_uri() throws IOException {

		var nodes = fileSystem.query("/test", node -> node).where("featured").is(true).get();
		
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("test/test1.md");
	}
}
