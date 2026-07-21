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

public class PersistentMetaDataUrlTest {

	@TempDir
	Path tempDirectory;

	@Test
	public void customUrlIsNormalizedAndOldMappingIsRemoved() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("content/page.md", Map.of(Constants.MetaFields.URL, "shop//item/"), LocalDate.now());

			var node = metadata.byUrl("/shop/item/");
			Assertions.assertThat(node).isPresent();
			Assertions.assertThat(node.get().path()).isEqualTo("content/page.md");
			Assertions.assertThat(node.get().url()).isEqualTo("/shop/item");

			metadata.addFile("content/page.md", Map.of(Constants.MetaFields.URL, "/new-item"), LocalDate.now());

			Assertions.assertThat(metadata.byUrl("/shop/item")).isEmpty();
			Assertions.assertThat(metadata.byUrl("/new-item")).isPresent();
		}
	}

	@Test
	public void lastDuplicateUrlWinsAndRemainsWhenLoserChangesUrl() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("first.md", Map.of(Constants.MetaFields.URL, "/duplicate"), LocalDate.now());
			metadata.addFile("second.md", Map.of(Constants.MetaFields.URL, "/duplicate"), LocalDate.now());

			Assertions.assertThat(metadata.byUrl("/duplicate")).get().extracting(node -> node.path()).isEqualTo("second.md");

			metadata.addFile("first.md", Map.of(Constants.MetaFields.URL, "/other"), LocalDate.now());

			Assertions.assertThat(metadata.byUrl("/duplicate")).get().extracting(node -> node.path()).isEqualTo("second.md");
		}
	}

	@Test
	public void previousDuplicateWinsWhenCurrentWinnerIsDeleted() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("first.md", Map.of(Constants.MetaFields.URL, "/duplicate"), LocalDate.now());
			metadata.addFile("second.md", Map.of(Constants.MetaFields.URL, "/duplicate"), LocalDate.now());

			metadata.removeFile("second.md");

			Assertions.assertThat(metadata.byUrl("/duplicate"))
					.get()
					.extracting(node -> node.path())
					.isEqualTo("first.md");
		}
	}
}
