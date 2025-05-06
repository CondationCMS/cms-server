package com.condation.cms.modules.ui.extensionpoints;

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
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;

/**
 *
 * @author t.marx
 */
@Extensions({
	@Extension(UIActionsExtensionPoint.class),
	@Extension(HookSystemRegisterExtensionPoint.class)
})
public class MenuExtension extends HookSystemRegisterExtensionPoint implements UIActionsExtensionPoint {

	@com.condation.cms.api.ui.annotations.MenuEntry(
			id = "parentDemo",
			name = "ParentDemo",
			position = 10
	)
	public void parentDefinition() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "demo-script-action",
			name = "ModalDemo",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/menu/action/test-modal")
	)
	@ShortCut(
		id = "demo-script-action",
		title = "Demo Open Modal",
		hotkey = "ctrl-1",
		section = "script"
	)
	public void test_modal() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "demo-script-action2",
			name = "SidebarDemo",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/menu/action/test-sidebar")
	)
	@ShortCut(
		id = "demo-script-action2",
		title = "Demo Open Sidebar",
		hotkey = "ctrl-2",
		section = "script"
	)
	public void test_sidebar() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "demo-hook-action",
			name = "HookDemo",
			position = 2
	)
	@Action("demo-hook-action")
	@ShortCut(
		id = "demo-hook-action",
		title = "Demo Call hook",
		hotkey = "ctrl-3",
		section = "hook"
	)
	public void demoScriptAction(ActionContext<?> context) {
		System.out.println("demo-hook-action called");
	}
}
