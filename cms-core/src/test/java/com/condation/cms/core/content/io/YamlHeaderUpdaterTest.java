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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlHeaderUpdaterTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void markdownFileIsReplacedWithoutLeavingTemporaryFiles() throws Exception {
		var markdownFile = temporaryDirectory.resolve("page.md");
		Files.writeString(markdownFile, "old content");

		YamlHeaderUpdater.saveMarkdownFileWithHeader(
				markdownFile, Map.of("title", "New title"), "New content");

		Assertions.assertThat(Files.readString(markdownFile))
				.contains("title: New title")
				.endsWith("New content\n");
		try (var files = Files.list(temporaryDirectory)) {
			Assertions.assertThat(files).containsExactly(markdownFile);
		}
	}
}
