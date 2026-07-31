package com.condation.cms.core.menu;

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

import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileMenuServiceTest {

	@TempDir
	Path siteDirectory;

	@Test
	void storesMenuAsYamlAndSupportsCrudOperations() throws Exception {
		var service = new FileMenuService(siteDirectory);
		var item = new MenuItem(
				"home",
				"link",
				"Startseite",
				"/",
				"_self",
				true,
				List.of());
		var menu = new Menu("main-navigation", "Hauptnavigation", List.of(item));

		service.create(menu);

		Path menuFile = siteDirectory.resolve("config/menus/main-navigation.yaml");
		Assertions.assertThat(menuFile).isRegularFile();
		Assertions.assertThat(Files.readString(menuFile))
				.contains("id: main-navigation")
				.contains("label: Startseite")
				.doesNotContain("{\"id\"")
				.doesNotContain("current")
				.doesNotContain("&id");
		Assertions.assertThat(service.get("main-navigation")).contains(menu);
		Assertions.assertThat(service.list()).containsExactly(menu);

		var updated = new Menu("main-navigation", "Main menu", List.of(item, item));
		service.update(updated);
		Assertions.assertThat(service.get("main-navigation")).contains(updated);

		Assertions.assertThat(service.delete("main-navigation")).isTrue();
		Assertions.assertThat(service.get("main-navigation")).isEmpty();
		Assertions.assertThat(service.delete("main-navigation")).isFalse();
	}

	@Test
	void rejectsIdsThatCouldEscapeTheMenuDirectory() {
		var service = new FileMenuService(siteDirectory);
		var menu = new Menu("../outside", "Outside", List.of());

		Assertions.assertThatThrownBy(() -> service.create(menu))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
