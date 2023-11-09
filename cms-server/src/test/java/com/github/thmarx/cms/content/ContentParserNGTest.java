package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ContentParserNGTest {
	
	public ContentParserNGTest() {
	}

	@Test
	public void testSomeMethod() throws IOException {
		var contentParser = new ContentParser(new FileSystem(Path.of("hosts/test/"), new DefaultEventBus()));
		
		var expectedMD = """
                   
                   Und hier der Inhalt
                   """;
		
		var content = contentParser.parse(Path.of("hosts/test/content/test.md"));
		
		Assertions.assertThat(content.meta()).containsKeys("title", "tags", "template");
		Assertions.assertThat(content.meta().get("title")).isEqualTo("Startseite");
		Assertions.assertThat(content.meta().get("tags")).isInstanceOf(List.class)
				.asList().containsExactly("eins", "zwei", "drei");
		Assertions.assertThat(content.content()).isEqualToIgnoringWhitespace(expectedMD);
	}
	
	@Test
	public void test_date() throws IOException {
		var contentParser = new ContentParser(new FileSystem(Path.of("hosts/test/"), new DefaultEventBus()));
		
		var content = contentParser.parse(Path.of("hosts/test/content/test.md"));
		
		Assertions.assertThat(content.meta().get("date")).isNotNull().isInstanceOf(Date.class);
		Assertions.assertThat((Date)content.meta().get("date"))
				.isAfter("2023-12-01")
				.isBefore("2023-12-03");
		
		Assertions.assertThat(content.meta().get("datetime")).isNotNull().isInstanceOf(Date.class);
		Assertions.assertThat((Date)content.meta().get("datetime"))
				.isAfter("2023-12-02T12:10:12")
				.isBefore("2023-12-02T15:10:12");
		
	}
	
	@Test
	public void test_tags() throws IOException {
		var contentParser = new ContentParser(new FileSystem(Path.of("hosts/test/"), new DefaultEventBus()));
		
		var content = contentParser.parse(Path.of("hosts/test/content/tags.md"));
		
		Assertions.assertThat(content.meta()).containsKey("tags");
		Assertions.assertThat(content.meta().get("tags")).isInstanceOf(List.class);
		Assertions.assertThat(content.meta().get("tags")).asList().containsExactly("eins", "zwei", "drei");
	}
}
