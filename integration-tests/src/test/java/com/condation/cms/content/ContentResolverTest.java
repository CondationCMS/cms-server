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


import com.condation.cms.TestDirectoryUtils;
import com.condation.cms.TestHelper;
import com.condation.cms.TestTemplateEngine;
import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.content.RedirectContentResponse;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.template.TemplateEngine;
import static com.condation.cms.content.ContentRendererNGTest.contentRenderer;
import static com.condation.cms.content.ContentRendererNGTest.moduleManager;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.test.TestSiteProperties;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author t.marx
 */
public class ContentResolverTest {

	static MarkdownRenderer markdownRenderer;
	static ContentResolver contentResolver;
	static FileDB db;

	@BeforeAll
	public static void setup() throws IOException {
		var contentParser = new DefaultContentParser();
		var hostBase = Path.of("target/test-" + System.currentTimeMillis());
		TestDirectoryUtils.copyDirectory(Path.of("hosts/test"), hostBase);
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
		contentResolver = new ContentResolver(contentRenderer, db);
	}

	@AfterAll
	public static void shutdown() throws Exception {
		db.close();
	}

	@Test
	public void test_hidden_folder() throws IOException {

		var context = TestHelper.requestContext(".technical/404");

		var optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isEmpty();
		optional = contentResolver.getErrorContent(context);
		Assertions.assertThat(optional).isPresent();
	}
	
	@Test
	public void testAliases() throws IOException {

		var context = TestHelper.requestContext("alias2");
		var optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isPresent();
		Assertions.assertThat(optional.get()).isInstanceOf(RedirectContentResponse.class);
		Assertions.assertThat(((RedirectContentResponse)optional.get()).location()).isEqualTo("/test");
		
		context = TestHelper.requestContext("alias3/sub1");
		optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isPresent();
		Assertions.assertThat(optional.get()).isInstanceOf(RedirectContentResponse.class);
		Assertions.assertThat(((RedirectContentResponse)optional.get()).location()).isEqualTo("/test");
	}
	
	@Test
	public void alias_no_redirect() throws IOException {

		var context = TestHelper.requestContext("alias_no_redirect");
		var optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isPresent();
		Assertions.assertThat(optional.get()).isInstanceOf(DefaultContentResponse.class);
		
		var defaultContent = (DefaultContentResponse)optional.get();
		Assertions.assertThat(defaultContent.content()).isNotNull();
		Assertions.assertThat(defaultContent.node().getMetaValue(Constants.MetaFields.TITLE, String.class).get()).isEqualTo("Alias without redirect");
	}
	
	@Test
	public void test_not_published() throws IOException {

		var context = TestHelper.requestContext("alias-hidden");
		var optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isEmpty();
		
		context = TestHelper.requestContext("hidden");
		optional = contentResolver.getContent(context);
		Assertions.assertThat(optional).isEmpty();
	}

}
