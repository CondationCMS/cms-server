package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * CMS FileSystem
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

import com.condation.cms.api.Constants;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PersistentMetaDataSectionIndexTest {

	private static final Map<String, Object> PUBLISHED = Map.of(Constants.MetaFields.STATUS, "published");

	@TempDir
	Path tempDirectory;

	@Test
	public void sectionsAreFoundByPagePathWhenPageHasCustomUrl() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("products/article.md", Map.of(Constants.MetaFields.URL, "/shop/summer"), LocalDate.now());
			metadata.addFile("products/article.hero.md", PUBLISHED, LocalDate.now());
			metadata.addFile("products/article.features.first.md", PUBLISHED, LocalDate.now());

			Assertions.assertThat(metadata.listSectionEntries("products/article.md"))
					.extracting(node -> node.path())
					.containsExactly(
							"products/article.features.first.md",
							"products/article.hero.md"
					);
		}
	}

	@Test
	public void sectionsOfOtherPagesAndFoldersAreNotReturned() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("products/article.hero.md", PUBLISHED, LocalDate.now());
			metadata.addFile("products/other.hero.md", PUBLISHED, LocalDate.now());
			metadata.addFile("archive/article.hero.md", PUBLISHED, LocalDate.now());

			Assertions.assertThat(metadata.listSectionEntries("products/article.md"))
					.singleElement()
					.extracting(node -> node.path())
					.isEqualTo("products/article.hero.md");
		}
	}
}
