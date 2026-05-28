package com.condation.cms.modules.ui.extensionpoints;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.ui.action.UIHookAction;
import com.condation.cms.api.ui.action.UIScriptAction;
import com.condation.cms.api.ui.elements.Menu;
import com.condation.cms.api.ui.elements.MenuEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author t.marx
 */
//@Extension(HookSystemRegisterExtensionPoint.class)
public class MenuHookExtension extends HookSystemRegisterExtensionPoint {

	@Override
	public void register(HookSystem hookSystem) {
		hookSystem.<Menu>registerFilter("module/ui/menu", (context) -> {
			var menu = context.value();
			menu.addMenuEntry(MenuEntry.builder()
					.children(new ArrayList<>(
							List.of(MenuEntry.builder().id("child1").name("ScriptAction")
									.position(0)
									.action(new UIScriptAction("/manager/actions/page/edit-content", Map.of("name", "CondationCMS")))
									.build(),
									MenuEntry.builder().id("div1").divider(true).position(1).build(),
									MenuEntry.builder().id("child2").name("HookAction")
											.position(2)
											.action(new UIHookAction("module/ui/demo/menu/action", Map.of("name", "CondationCMS")))
											.build()
							)))
				.name("ExampleMenu")
				.id("example-menu")
				.build());
			return menu;
		});

		hookSystem.registerAction("module/ui/demo/menu/action", context -> {
			System.out.println("hook action executed");
			System.out.println("hello " + context.arguments().get("name"));
			return "";
		});
	}
}
