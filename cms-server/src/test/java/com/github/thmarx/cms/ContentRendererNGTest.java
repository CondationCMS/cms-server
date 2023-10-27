package com.github.thmarx.cms;

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

import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.template.TemplateEngineTest;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class ContentRendererNGTest extends TemplateEngineTest {
	
	ContentRenderer contentRenderer;
	private MarkdownRenderer markdownRenderer;
	
	@BeforeClass
	public void beforeClass () throws IOException {
		final FileSystem fileSystem = new FileSystem(Path.of("hosts/test/"), new EventBus());
		var contentParser = new ContentParser(fileSystem);
		markdownRenderer = TestHelper.getRenderer();
		TemplateEngine templates = new FreemarkerTemplateEngine(fileSystem, contentParser);
		
		contentRenderer = new ContentRenderer(contentParser, templates, fileSystem);
	}

	@Test
	public void testSomeMethod() throws IOException {
		var expectedHTML = """
                     <html>
                     	<head>
                     		<title>Startseite</title>
                     	</head>
						<body>
                            <p>Und hier der Inhalt</p>
						</body>
                     </html>
                     """;
		var content = contentRenderer.render(Path.of("hosts/test/content/test.md"), requestContext());
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
	
	@Test
	public void test_subfolder() throws IOException {
		var expectedHTML = """
                     <html>
                     	<head>
                     		<title>ProduktSeite</title>
                     	</head>
						<body>
							<p>Das ist ein Produkt!</p>
                     	</body>
                     </html>
                     """;
		var content = contentRenderer.render(Path.of("hosts/test/content/products/test.md"), requestContext());
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
}
