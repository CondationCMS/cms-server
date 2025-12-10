package com.condation.cms.modules.example.ui;

/*-
 * #%L
 * example-module
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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.ui.annotations.MenuEntry;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.cms.api.ui.extensions.UIScriptActionSourceExtension;
import com.condation.cms.api.utils.ClasspathResourceLoader;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;
import java.util.Map;

/**
 *
 * @author thmar
 */
@Extensions({
	@Extension(UIActionsExtensionPoint.class),
	@Extension(UIScriptActionSourceExtension.class)
})
public class ExampleUiScriptActionSourceExtension extends AbstractExtensionPoint implements UIScriptActionSourceExtension, UIActionsExtensionPoint {

	@Override
	public Map<String, String> getActionSources() {
		return Map.of("example/source", ClasspathResourceLoader.loadRelative(ExampleUiScriptActionSourceExtension.class, "example-action.js"));
	}

	@MenuEntry(
			id = "exampleMenu",
			name = "Example",
			permissions = {Permissions.CONTENT_EDIT},
			position = 10
	)
	public void exampleMenu() {

	}

	@MenuEntry(
			parent = "exampleMenu",
			id = "example-action",
			name = "Example action",
			permissions = {Permissions.CONTENT_EDIT},
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/example/source")
	)
	@ShortCut(
			id = "example-action",
			title = "Example Action",
			permissions = {Permissions.CONTENT_EDIT},
			section = "Example",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/example/source")
	)
	public void example_action() {

	}
}
