package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.ui.action.HookAction;
import com.condation.cms.api.ui.menu.Menu;
import com.condation.cms.api.ui.menu.MenuEntry;
import com.condation.cms.core.cache.LocalCacheProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class TemplateEngineTest {

	@Test
	public void testSomeMethod() {
		CacheManager cacheManager = new CacheManager(new LocalCacheProvider());
		TemplateEngine templateEngine = new TemplateEngine(cacheManager);

		var hookSystem = new HookSystem();
		hookSystem.registerFilter("module/ui/menu", (FilterContext<Menu> context)
				-> {
			var menu = context.value();
			menu.addMenuEntry(MenuEntry.builder()
					.children(new ArrayList<>(
							List.of(
									MenuEntry.builder().id("child1").name("Child 1").position(0).build(),
									MenuEntry.builder().id("div1").divider(true).position(1).build(),
									MenuEntry.builder().id("child2").name("Child 2")
											.position(2)
											.action(new HookAction("module/ui/demo/menu/action", Map.of("name", "CondationCMS")))
											.build()
							)))
					.name("ExampleMenu")
					.id("example-menu")
					.build());

			return menu;
		}
		);

		Assertions.assertThatCode(() -> {
			templateEngine.render("index.html", Map.of("uihooks", new UIHooks(hookSystem)));
		}).doesNotThrowAnyException();
	}

}
