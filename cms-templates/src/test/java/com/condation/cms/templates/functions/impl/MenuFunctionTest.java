package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuItem;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.hooksystem.CMSHookSystem;
import com.google.inject.Injector;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MenuFunctionTest {

	@Test
	void marksCurrentItemsOnRequestLocalCopies() throws Exception {
		Menu cachedMenu = new Menu("main", "Main", List.of(
				new MenuItem("home", "link", "Home", "/", "_self", true, List.of()),
				new MenuItem("section", "heading", "Products", "", "_self", true, List.of(
						new MenuItem("product", "link", "Product", "/products/?source=menu#top", "_self", true, List.of())))));
		MenuService menuService = Mockito.mock(MenuService.class);
		Mockito.when(menuService.get("main")).thenReturn(Optional.of(cachedMenu));
		Injector injector = Mockito.mock(Injector.class);
		Mockito.when(injector.getInstance(MenuService.class)).thenReturn(menuService);

		Menu productsMenu = invoke(injector, "/products");
		Menu homeMenu = invoke(injector, "/");

		Assertions.assertThat(productsMenu.items().get(0).current()).isFalse();
		Assertions.assertThat(productsMenu.items().get(1).children().get(0).current()).isTrue();
		Assertions.assertThat(homeMenu.items().get(0).current()).isTrue();
		Assertions.assertThat(homeMenu.items().get(1).children().get(0).current()).isFalse();

		Assertions.assertThat(cachedMenu.items().get(0).current()).isFalse();
		Assertions.assertThat(cachedMenu.items().get(1).children().get(0).current()).isFalse();
	}

	@Test
	void omitsDisabledItemsRecursively() throws Exception {
		Menu cachedMenu = new Menu("main", "Main", List.of(
				new MenuItem("visible", "heading", "Visible", "", "_self", true, List.of(
						new MenuItem("hidden-child", "link", "Hidden", "/hidden", "_self", false, List.of()))),
				new MenuItem("hidden-parent", "heading", "Hidden", "", "_self", false, List.of(
						new MenuItem("visible-child", "link", "Visible", "/visible", "_self", true, List.of())))));
		MenuService menuService = Mockito.mock(MenuService.class);
		Mockito.when(menuService.get("main")).thenReturn(Optional.of(cachedMenu));
		Injector injector = Mockito.mock(Injector.class);
		Mockito.when(injector.getInstance(MenuService.class)).thenReturn(menuService);

		Menu menu = invoke(injector, "/");

		Assertions.assertThat(menu.items()).extracting(MenuItem::id)
				.containsExactly("visible");
		Assertions.assertThat(menu.items().getFirst().children()).isEmpty();
	}

	@Test
	void filtersMenuItemsUsingModifiableLists() throws Exception {
		Menu cachedMenu = new Menu("main", "Main", List.of(
				new MenuItem("home", "link", "Home", "/", "_self", true, List.of()),
				new MenuItem("products", "heading", "Products", "", "_self", true, List.of())));
		MenuService menuService = Mockito.mock(MenuService.class);
		Mockito.when(menuService.get("main")).thenReturn(Optional.of(cachedMenu));
		Injector injector = Mockito.mock(Injector.class);
		Mockito.when(injector.getInstance(MenuService.class)).thenReturn(menuService);

		CMSHookSystem hookSystem = new CMSHookSystem();
		hookSystem.registerFilter(Hooks.MENU_FILTER.hook("main"),
				(FilterContext<List<MenuItem>> context) -> {
					context.value().add(new MenuItem(
							"contact", "link", "Contact", "/contact", "_self", true, List.of()));
					context.value().add(new MenuItem(
							"hidden", "link", "Hidden", "/hidden", "_self", false, List.of()));
					context.value().get(1).children().add(new MenuItem(
							"product", "link", "Product", "/products/product", "_self", true, List.of()));
					context.value().get(1).children().add(new MenuItem(
							"hidden-product", "link", "Hidden product", "/products/hidden", "_self", false, List.of()));
					return context.value();
				});

		RequestContext context = new RequestContext();
		context.add(InjectorFeature.class, new InjectorFeature(injector));
		context.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));

		Menu menu = (Menu) new MenuFunction(context).invoke("main");

		Assertions.assertThat(menu.items()).extracting(MenuItem::id)
				.containsExactly("home", "products", "contact");
		Assertions.assertThat(menu.items().get(1).children()).extracting(MenuItem::id)
				.containsExactly("product");
	}

	private Menu invoke(Injector injector, String currentUrl) {
		RequestContext context = new RequestContext();
		context.add(InjectorFeature.class, new InjectorFeature(injector));
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(
				new ContentNode(currentUrl, currentUrl, currentUrl, Map.of())));
		return (Menu) new MenuFunction(context).invoke("main");
	}
}
