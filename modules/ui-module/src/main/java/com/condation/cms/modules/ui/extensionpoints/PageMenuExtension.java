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
import com.condation.cms.api.auth.Permissions;
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

//	@com.condation.cms.api.ui.annotations.MenuEntry(
//			id = "pageMenu",
//			name = "Page",
//			position = 10
//	)
//	public void parentDefinition() {
//
//	}

	/*
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "page-create",
			name = "Create new page",
			position = 1,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/create-page")
	)*/
	@ShortCut(
			id = "page-create",
			title = "Create new page",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-3",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/create-page")
	)
	public void create_page() {

	}
	/*
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "page-edit-meta",
			name = "Edit MetaData",
			position = 3,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/edit-page-settings")
	)*/
	@ShortCut(
			id = "page-edit-meta",
			title = "Edit page settings",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-2",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/edit-page-settings")
	)
	public void page_settings() {}
	/*
	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "pageMenu",
			id = "manage-assets",
			name = "Manage assets",
			position = 10,
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/manage-assets")
	)
	*/
	@ShortCut(
			id = "manager-assets",
			title = "Manage assets",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-4",
			section = "Assets",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/manage-assets")
	)
	public void manage_media() {

	}
	
	@ShortCut(
			id = "page-edit-translations",
			title = "Edit page translations",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-5",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/translations")
	)
	public void manage_translations() {}

	@ShortCut(
			id = "page-variants",
			title = "Page variants",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-6",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/variants")
	)
	public void page_variants() {}

	@ShortCut(
			id = "page-variant-create",
			title = "Create page variant",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-7",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/create-variant")
	)
	public void create_page_variant() {}

	@ShortCut(
			id = "page-variant-selector",
			title = "Configure variant selection",
			permissions = {Permissions.CONTENT_EDIT},
			hotkey = "ctrl-8",
			section = "Page",
			scriptAction = @com.condation.cms.api.ui.annotations.ScriptAction(module = "/manager/actions/page/variant-selector")
	)
	public void configure_variant_selector() {}
	

	@Override
	public Map<String, Map<String, String>> getLocalizations() {
		return Map.of(
				"de", Map.of(
						"pageMenu", "Seite",
						"page-create", "Neue Seite erstellen",
						"page-edit-content", "Inhalt bearbeiten",
						"page-edit-meta", "Metadaten bearbeiten",
						"page-variants", "Seitenvarianten",
						"page-variant-create", "Seitenvariante erstellen",
						"page-variant-selector", "Variantenauswahl konfigurieren",
						"language.de", "Deutsch",
						"language.en", "Englisch"
				),
				"en", Map.of(
						"pageMenu", "Page",
						"page-create", "Create new page",
						"page-edit-content", "Edit content",
						"page-edit-meta", "Edit metadata",
						"page-variants", "Page variants",
						"page-variant-create", "Create page variant",
						"page-variant-selector", "Configure variant selection",
						"language.de", "German",
						"language.en", "English"
				)
		);
	}
}
