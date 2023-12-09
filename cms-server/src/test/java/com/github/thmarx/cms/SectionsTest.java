package com.github.thmarx.cms;

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
import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.content.ContentRenderer;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.filesystem.FileDB;
import com.github.thmarx.cms.template.TemplateEngineTest;
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

	static ContentRenderer contentRenderer;
	static MarkdownRenderer markdownRenderer;
	static FileDB db;

	@BeforeAll
	public static void beforeClass() throws IOException {
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
				() -> new MockModuleManager()
		);
	}

	@Test
	public void test_sections() throws IOException {
		List<ContentNode> listSections = db.getContent().listSections(db.getFileSystem().resolve("content/page.md"));
		Assertions.assertThat(listSections).hasSize(4);

		Map<String, List<ContentRenderer.Section>> renderSections = contentRenderer.renderSections(listSections, TestHelper.requestContext());

		Assertions.assertThat(renderSections)
				.hasSize(1)
				.containsKey("left");

		Assertions.assertThat(renderSections.get("left"))
				.hasSize(4);

		var sectionIndexes = renderSections.get("left").stream().map(section -> section.index()).collect(Collectors.toList());
		Assertions.assertThat(sectionIndexes).containsExactly(0, 1, 2, 10);
	}
}
