package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.TestHelper;
import com.github.thmarx.cms.TestTemplateEngine;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.template.TemplateEngine;
import static com.github.thmarx.cms.content.ContentRendererNGTest.contentRenderer;
import static com.github.thmarx.cms.content.ContentRendererNGTest.moduleManager;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import com.github.thmarx.cms.filesystem.FileDB;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ContentResolverTest {
	
	static MarkdownRenderer markdownRenderer;
	static ContentResolver contentResolver;
	static FileDB db;

	@BeforeAll
	public static void setup () throws IOException {
				var contentParser = new ContentParser();
		db = new FileDB(Path.of("hosts/test/"), new DefaultEventBus(), (file) -> {
			try {
				return contentParser.parseMeta(file);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		db.init();
		markdownRenderer = TestHelper.getRenderer();
		TemplateEngine templates = new TestTemplateEngine(db);
		
		contentRenderer = new ContentRenderer(contentParser, 
				() -> templates, 
				db, 
				new SiteProperties(Map.of()), 
				() -> moduleManager);
		contentResolver = new ContentResolver(db.getFileSystem().resolve("content/"), contentRenderer, db);
	}
	@AfterAll
	public static void shutdown () throws Exception {
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
	
}
