package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.core.menu.FileMenuService;
import com.google.inject.Guice;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RemoteMenuEndpointsTest {

	@TempDir
	Path siteDirectory;

	@Test
	void exposesAllMenuCrudOperations() throws Exception {
		MenuService service = new FileMenuService(siteDirectory);
		var injector = Guice.createInjector(binder ->
				binder.bind(MenuService.class).toInstance(service));
		var context = new SiteModuleContext();
		context.add(InjectorFeature.class, new InjectorFeature(injector));
		var endpoints = new RemoteMenuEndpoints();
		endpoints.setContext(context);
		var menu = new Menu("main", "Main navigation", List.of());
		var menuJson = Map.of(
				"id", "main",
				"name", "Main navigation",
				"items", List.of());
		var parameters = Map.<String, Object>of();

		Assertions.assertThat(endpoints.list(parameters)).isEqualTo(List.of());

		Object created = endpoints.create(Map.of("menu", menuJson));
		Assertions.assertThat(created).isEqualTo(menu);
		Assertions.assertThat(endpoints.get(Map.of("id", "main"))).isEqualTo(menu);

		var updated = new Menu("main", "Updated navigation", List.of());
		Assertions.assertThat(endpoints.update(Map.of("menu", updated))).isEqualTo(updated);
		Assertions.assertThat(endpoints.list(parameters)).isEqualTo(List.of(updated));

		Assertions.assertThat(endpoints.delete(Map.of("id", "main")))
				.isEqualTo(Map.of("deleted", true));
		Assertions.assertThat(endpoints.list(parameters)).isEqualTo(List.of());
	}
}
