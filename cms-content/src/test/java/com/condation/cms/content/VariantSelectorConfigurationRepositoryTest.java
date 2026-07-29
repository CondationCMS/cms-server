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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VariantSelectorConfigurationRepositoryTest {

	@TempDir
	Path contentBase;

	private final ContentNode canonical = new ContentNode(
			"news/about.md", "/news/about", "about.md", Map.of()
	);
	private VariantSelectorConfigurationRepository repository;

	@BeforeEach
	void setUp() {
		var db = mock(DB.class);
		var fileSystem = mock(DBFileSystem.class);
		var resolver = mock(VariantResolver.class);
		when(db.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.resolve(Constants.Folders.CONTENT)).thenReturn(contentBase);
		when(resolver.resolveContext(canonical)).thenReturn(
				new VariantResolver.VariantContext(canonical, Optional.empty(), List.of())
		);
		repository = new VariantSelectorConfigurationRepository(db, resolver);
	}

	@Test
	void missingConfigurationUsesDateRangeDefault() {
		assertThat(repository.getSelectorId(canonical))
				.isEqualTo(VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID);
	}

	@Test
	void storesOneConfigurationAtCanonicalVariantFolder() throws Exception {
		repository.setSelectorId(canonical, "audience");

		var file = contentBase.resolve("news/.variants/about/variants.yaml");
		assertThat(repository.configurationFile(canonical)).isEqualTo(file);
		assertThat(Files.readString(file)).contains("selector: audience");
		assertThat(repository.getSelectorId(canonical)).isEqualTo("audience");
	}
}
