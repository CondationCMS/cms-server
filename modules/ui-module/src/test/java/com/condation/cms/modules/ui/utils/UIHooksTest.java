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
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.ui.elements.Menu;
import com.condation.cms.api.ui.elements.MenuEntry;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class UIHooksTest {

	@Test
	public void registerMenuEntry() {

		HookSystem hookSystem = new HookSystem();

		hookSystem.registerFilter(UIHooks.HOOK_MENU, (FilterContext<Menu> context) -> {
			var menu = context.value();
			
			menu.addMenuEntry(MenuEntry.builder()
					.children(new ArrayList<>(List.of(MenuEntry.builder().id("child1").name("Child1").build())))
					.name("Parent1")
					.id("p1")
					.build());
			
			return menu;
		});

		UIHooks uiHooks = new UIHooks(hookSystem);

		var entries = uiHooks.menu().entries();
		Assertions.assertThat(entries).hasSize(1);
		Assertions.assertThat(entries.getFirst().getChildren()).hasSize(1);
	}

	@Test
	public void registerMenuEntryAreSorted() {

		HookSystem hookSystem = new HookSystem();

		hookSystem.registerFilter(UIHooks.HOOK_MENU, (FilterContext<Menu> context) -> {
			var menu = context.value();
			
			menu.addMenuEntry(MenuEntry.builder()
					.id("parent55")
					.name("parent55")
					.position(55)
					.build());
			menu.addMenuEntry(MenuEntry.builder()
					.name("parent66")
					.id("parent66")
					.position(66)
					.build());
			menu.addMenuEntry(MenuEntry.builder()
					.name("parent44")
					.id("parent44")
					.position(44)
					.build());
			
			return menu;
		});

		UIHooks uiHooks = new UIHooks(hookSystem);

		var entries = uiHooks.menu().entries();
		Assertions.assertThat(entries).hasSize(3);
		Assertions.assertThat(entries.get(0).getName()).isEqualTo("parent44");
		Assertions.assertThat(entries.get(1).getName()).isEqualTo("parent55");
		Assertions.assertThat(entries.get(2).getName()).isEqualTo("parent66");
	}
}
