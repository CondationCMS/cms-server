package com.condation.cms.core.content.io;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContentFileParserTest {

	@TempDir
	private Path tempDir;

	@Test
	void returnsMutableEmptyHeaderWhenFrontmatterIsMissing() throws Exception {
		Path markdown = tempDir.resolve("without-frontmatter.md");
		Files.writeString(markdown, "Body");

		var parser = new ContentFileParser(markdown.toString());

		Assertions.assertThat(parser.getHeader()).isEmpty();
		parser.getHeader().put("title", "Added later");
		Assertions.assertThat(parser.getHeader()).containsEntry("title", "Added later");
	}

	@Test
	void returnsMutableEmptyHeaderWhenFrontmatterIsEmpty() throws Exception {
		Path markdown = tempDir.resolve("empty-frontmatter.md");
		Files.writeString(markdown, "---\n---\nBody");

		var parser = new ContentFileParser(markdown.toString());

		Assertions.assertThat(parser.getHeader()).isEmpty();
		parser.getHeader().put("title", "Added later");
		Assertions.assertThat(parser.getHeader()).containsEntry("title", "Added later");
	}
}
