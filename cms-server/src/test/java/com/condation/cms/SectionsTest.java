package com.condation.cms;

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


import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.content.DefaultContentParser;
import com.condation.cms.content.DefaultContentRenderer;
import com.condation.cms.content.Section;
import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.template.TemplateEngineTest;
import com.condation.cms.test.TestSiteProperties;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class SectionsTest extends TemplateEngineTest {

	static DefaultContentRenderer contentRenderer;
	static MarkdownRenderer markdownRenderer;
	static FileDB db;

	@BeforeAll
	public static void beforeClass() throws IOException {
		var contentParser = new DefaultContentParser();
		var hostBase = Path.of("hosts/test/");
		var config = new Configuration(Path.of("hosts/test/"));
		db = new FileDB(Path.of("hosts/test/"), new DefaultEventBus(), (file) -> {
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
				new MockModuleManager()
		);
	}

	@Test
	public void test_sections() throws IOException {
		List<ContentNode> listSections = db.getContent().listSections(db.getReadOnlyFileSystem().contentBase().resolve("page.md"));
		Assertions.assertThat(listSections).hasSize(4);

		Map<String, List<Section>> renderSections = contentRenderer.renderSections(listSections, TestHelper.requestContext());

		Assertions.assertThat(renderSections)
				.hasSize(1)
				.containsKey("left");

		Assertions.assertThat(renderSections.get("left"))
				.hasSize(4);

		var sectionIndexes = renderSections.get("left").stream().map(section -> section.index()).collect(Collectors.toList());
		Assertions.assertThat(sectionIndexes).containsExactly(0, 1, 2, 10);
	}
}
