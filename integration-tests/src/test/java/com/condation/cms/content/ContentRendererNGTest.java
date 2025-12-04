package com.condation.cms.content;

/*-
 * #%L
 * cms-server
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



import com.condation.cms.MockModuleManager;
import com.condation.cms.TestDirectoryUtils;
import com.condation.cms.TestHelper;
import com.condation.cms.TestTemplateEngine;
import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.template.TemplateEngineTest;
import com.condation.cms.test.TestSiteProperties;
import com.condation.modules.api.ModuleManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class ContentRendererNGTest extends TemplateEngineTest {
	
	static DefaultContentRenderer contentRenderer;
	static MarkdownRenderer markdownRenderer;
	
	static ModuleManager moduleManager = new MockModuleManager();
	static FileDB db;
	static Path hostBase;
	
	@BeforeAll
	public static void beforeClass () throws IOException {
		
		hostBase = Path.of("target/test-" + System.currentTimeMillis());
		TestDirectoryUtils.copyDirectory(Path.of("hosts/test"), hostBase);
		
		var contentParser = new DefaultContentParser();
		var config = new Configuration();
		var siteConfigMock = Mockito.mock(SiteConfiguration.class);
		var sitePropsMock = Mockito.mock(SiteProperties.class);
		Mockito.when(sitePropsMock.id()).thenReturn("test-site");
		Mockito.when(siteConfigMock.siteProperties()).thenReturn(sitePropsMock);
		config.add(SiteConfiguration.class, siteConfigMock);
		db = new FileDB(hostBase, new DefaultEventBus(), (file) -> {
			try {
				ReadOnlyFile cmsFile = new NIOReadOnlyFile(file, hostBase.resolve(Constants.Folders.CONTENT));
				return contentParser.parseMeta(cmsFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, config);
		db.init();
		markdownRenderer = TestHelper.getRenderer();
		TemplateEngine templates = new TestTemplateEngine(db);
		
		contentRenderer = new DefaultContentRenderer(contentParser, 
				() -> templates, 
				db, 
				new TestSiteProperties(Map.of()), 
				moduleManager);
	}
	@AfterAll
	public static void shutdown () throws Exception {
		db.close();
	}

	@Test
	public void testSomeMethod() throws IOException {
		var expectedHTML = """
                     <html>
                     	<head>
                     		<title>StartseiteView</title>
                     	</head>
						<body>
                            Und hier der Inhalt
						</body>
                     </html>
                     """;
		var content = contentRenderer.render(new NIOReadOnlyFile(Path.of("hosts/test/content/test.md"), Path.of("hosts/test/"))
				, TestHelper.requestContext());
		
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
							Das ist ein Produkt!
                     	</body>
                     </html>
                     """;
		var content = contentRenderer.render(new NIOReadOnlyFile(Path.of("hosts/test/content/products/test.md"), Path.of("hosts/test/"))
				, TestHelper.requestContext());
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
}
