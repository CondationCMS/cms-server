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
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.cms.api.ui.extensions.UILocalizationExtensionPoint;
import java.util.Map;

/**
 *
 * @author t.marx
 */
@Extensions({
	@Extension(UIActionsExtensionPoint.class),
	@Extension(HookSystemRegisterExtensionPoint.class),
	@Extension(UILocalizationExtensionPoint.class)
})
public class PageMenuExtension extends HookSystemRegisterExtensionPoint implements UIActionsExtensionPoint, UILocalizationExtensionPoint {

	@com.condation.cms.api.ui.annotations.MenuEntry(
			id = "pageMenu",
			name = "Page",
			position = 10
	)
	public void parentDefinition() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "page-create",
			name = "Create new page",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/create-page")
	)
	@ShortCut(
			id = "page-create",
			title = "Create new page",
			hotkey = "ctrl-3",
			section = "Page"
	)
	public void create_page() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "page-edit-content",
			name = "Edit Content",
			position = 2,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/edit-content")
	)
	@ShortCut(
			id = "page-edit-content",
			title = "Edit Content",
			hotkey = "ctrl-1",
			section = "Page"
	)
	public void test_modal() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "page-edit-meta",
			name = "Edit MetaData",
			position = 3,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/edit-meta")
	)
	@ShortCut(
			id = "page-edit-meta",
			title = "Edit MetaData",
			hotkey = "ctrl-2",
			section = "Page"
	)
	public void test_sidebar() {

	}
	
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "manage-assets",
			name = "Manage assets",
			position = 10,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/manage-assets")
	)
	@ShortCut(
			id = "manager-asets",
			title = "Manage assets",
			hotkey = "ctrl-4",
			section = "Assets"
	)
	public void manage_media() {

	}
	/*
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "parentDemo",
			id = "test-get-contentNode",
			name = "GetContentNode",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/test-command")
	)
	@ShortCut(
		id = "test-get-contentNode",
		title = "GetContentNode",
		section = "script"
	)
	public void get_Content_node() {

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
	 */

	@Override
	public Map<String, Map<String, String>> getLocalizations() {
		return Map.of(
				"de", Map.of(
						"pageMenu", "Seite",
						"page-create", "Neue Seite erstellen",
						"page-edit-content", "Inhalt bearbeiten",
						"page-edit-meta", "Metadaten bearbeiten",
						"language.de", "Deutsch",
						"language.en", "Englisch"
				),
				"en", Map.of(
						"pageMenu", "Page",
						"page-create", "Create new page",
						"page-edit-content", "Edit content",
						"page-edit-meta", "Edit metadata",
						"language.de", "German",
						"language.en", "English"
				)
		);
	}
}
