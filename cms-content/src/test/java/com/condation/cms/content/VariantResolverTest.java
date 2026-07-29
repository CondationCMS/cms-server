package com.condation.cms.content;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VariantResolverTest {

	@Test
	void resolvesCanonicalPageAndActiveVariantFromVariantPath() {
		var db = mock(DB.class);
		var content = mock(Content.class);
		var fileSystem = mock(DBFileSystem.class);
		var contentBase = mock(ReadOnlyFile.class);
		var canonicalFile = mock(ReadOnlyFile.class);
		var canonicalFolder = mock(ReadOnlyFile.class);
		var variantsFolder = mock(ReadOnlyFile.class);
		var canonical = node("news/about.md", "/news/about", "about.md");
		var variant = node(
				"news/.variants/about/summer/about.md",
				"/news/.variants/about/summer/about",
				"about.md"
		);

		when(db.getContent()).thenReturn(content);
		when(db.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.contentBase()).thenReturn(contentBase);
		when(content.byPath("news/about.md")).thenReturn(Optional.of(canonical));
		when(contentBase.resolve("news/about.md")).thenReturn(canonicalFile);
		when(canonicalFile.getParent()).thenReturn(canonicalFolder);
		when(canonicalFolder.resolve(".variants/about")).thenReturn(variantsFolder);
		when(variantsFolder.exists()).thenReturn(false);

		var context = new VariantResolver(db).resolveContext(variant);

		assertThat(context.canonical()).isEqualTo(canonical);
		assertThat(context.activeVariantId()).contains("summer");
		assertThat(context.variants()).isEmpty();
	}

	private ContentNode node(String path, String url, String name) {
		return new ContentNode(path, url, name, Map.of("title", name));
	}
}
