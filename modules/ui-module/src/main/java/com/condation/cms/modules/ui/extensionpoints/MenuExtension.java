package com.condation.cms.modules.ui.extensionpoints;

/*-
 * #%L
 * ui-module-demo
 * %%
 * Copyright (C) 2024 Marx-Software
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
import com.condation.cms.api.ui.extensions.UIMenuExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;

/**
 *
 * @author t.marx
 */
@Extensions({
		@Extension(UIMenuExtensionPoint.class),
		@Extension(HookSystemRegisterExtensionPoint.class)
})
public class MenuExtension extends HookSystemRegisterExtensionPoint implements UIMenuExtensionPoint {

	@com.condation.cms.api.ui.annotations.MenuEntry(
			id = "parentDemo",
			name = "ParentDemo",
			position = 10
	)
	public void parentDefinition () {
		
	}
	
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "demo-script-action",
			name = "ScriptDemo",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/menu/action/test")
	)
	public void demoScriptAction () {
		
	}
	
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "demo-hook-action",
			name = "HookDemo",
			position = 2
	)
	@Action("demo-hook-action")
	public void demoScriptAction (ActionContext<?> context) {
		System.out.println("demo-hook-action called");
	}
}
