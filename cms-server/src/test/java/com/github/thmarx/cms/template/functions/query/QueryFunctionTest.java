package com.github.thmarx.cms.template.functions.query;

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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.cache.CacheManager;
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.db.cms.NIOReadOnlyFile;
import com.github.thmarx.cms.api.db.cms.ReadOnlyFile;
import com.github.thmarx.cms.api.mapper.ContentNodeMapper;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.content.DefaultContentParser;
import com.github.thmarx.cms.core.cache.LocalCacheProvider;
import com.github.thmarx.cms.core.eventbus.DefaultEventBus;
import com.github.thmarx.cms.filesystem.FileDB;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class QueryFunctionTest {

	static QueryFunction query;
	private static FileDB db;
	static MarkdownRenderer markdownRenderer = TestHelper.getRenderer();

	@BeforeAll
	static void init() throws IOException {
		var hostBase = Path.of("hosts/test/");
		var contentParser = new DefaultContentParser(new CacheManager(new LocalCacheProvider()));
		var config = new Configuration(Path.of("hosts/test/"));
		db = new FileDB(Path.of("hosts/test"), new DefaultEventBus(), (file) -> {
			try {
				ReadOnlyFile cmsFile = new NIOReadOnlyFile(file, hostBase.resolve(Constants.Folders.CONTENT));
				return contentParser.parseMeta(cmsFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, config);
		db.init();
		defaultContentParser = new DefaultContentParser(new CacheManager(new LocalCacheProvider()));
		query = new QueryFunction(db, 
				new NIOReadOnlyFile(Path.of("hosts/test/content/nav/index.md"), hostBase), 
				TestHelper.requestContext("/", defaultContentParser, markdownRenderer, new ContentNodeMapper(db, defaultContentParser)));
	}
	protected static DefaultContentParser defaultContentParser;

	@Test
	public void testSomeMethod() {

		Assertions.assertThat(query.toUrl("index.md")).isEqualTo("/");
		Assertions.assertThat(query.toUrl("test.md")).isEqualTo("/test");
		Assertions.assertThat(query.toUrl("demo/test.md")).isEqualTo("/demo/test");
		Assertions.assertThat(query.toUrl("demo/index.md")).isEqualTo("/demo");

	}

}
