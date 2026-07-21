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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PersistentMetaDataIncrementalDeleteTest {

	private static final Map<String, Object> PUBLISHED = Map.of(Constants.MetaFields.STATUS, "published");

	@TempDir
	Path tempDirectory;

	@Test
	public void removeDirectoryOnlyRemovesTheSelectedSubtree() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.createDirectory("archive");
			metadata.createDirectory("archive/nested");
			metadata.createDirectory("archive-other");
			metadata.addFile("archive/page.md", page("/old-page"), LocalDate.now());
			metadata.addFile("archive/page.hero.md", PUBLISHED, LocalDate.now());
			metadata.addFile("archive/nested/child.md", page("/old-child"), LocalDate.now());
			metadata.addFile("archive-other/page.md", page("/still-there"), LocalDate.now());

			metadata.removePath("archive");

			Assertions.assertThat(metadata.byPath("archive/page.md")).isEmpty();
			Assertions.assertThat(metadata.byPath("archive/nested/child.md")).isEmpty();
			Assertions.assertThat(metadata.byUrl("/old-page")).isEmpty();
			Assertions.assertThat(metadata.byUrl("/old-child")).isEmpty();
			Assertions.assertThat(metadata.listSectionEntries("archive/page.md")).isEmpty();
			Assertions.assertThat(metadata.findFolder("archive")).isEmpty();

			Assertions.assertThat(metadata.byPath("archive-other/page.md")).isPresent();
			Assertions.assertThat(metadata.byUrl("/still-there")).isPresent();
			Assertions.assertThat(metadata.findFolder("archive-other")).isPresent();
			Assertions.assertThat(metadata.query((node, count) -> node.path()).get())
					.containsExactly("archive-other/page.md");
		}
	}

	@Test
	public void directoryRenameCanBeProcessedAsDeleteAndCreateOfSubtree() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.createDirectory("old");
			metadata.createDirectory("old/nested");
			metadata.addFile("old/page.md", PUBLISHED, LocalDate.now());
			metadata.addFile("old/nested/child.md", PUBLISHED, LocalDate.now());

			metadata.removePath("old");
			metadata.createDirectory("new");
			metadata.createDirectory("new/nested");
			metadata.addFile("new/page.md", PUBLISHED, LocalDate.now());
			metadata.addFile("new/nested/child.md", PUBLISHED, LocalDate.now());

			Assertions.assertThat(metadata.getNodes()).containsOnlyKeys("new/page.md", "new/nested/child.md");
			Assertions.assertThat(metadata.byUrl("/old/page")).isEmpty();
			Assertions.assertThat(metadata.byUrl("/new/page")).isPresent();
			Assertions.assertThat(metadata.findFolder("old")).isEmpty();
			Assertions.assertThat(metadata.findFolder("new/nested")).isPresent();
		}
	}

	private static Map<String, Object> page(String url) {
		return Map.of(
				Constants.MetaFields.STATUS, "published",
				Constants.MetaFields.URL, url
		);
	}
}
