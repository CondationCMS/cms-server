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
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.ui.elements.ContentTypes;
import com.condation.cms.api.ui.elements.MediaForms;
import com.condation.cms.api.ui.elements.Menu;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public class UIHooks {

	public static final String HOOK_MENU = "module/ui/menu";
	public static final String HOOK_TRANSLATIONS = "module/ui/translations";
	public static final String HOOK_REGISTER_CONTENT_TYPES = "manager/contentTypes/register";
	public static final String HOOK_REGISTER_MEDIA_FORMS = "manager/media/forms";

	private final HookSystem hookSystem;
	
	public UIHooks (final HookSystem hookSystem) {
		this.hookSystem = hookSystem;
	}

	public ContentTypes contentTypes () {
		var contentTypes = new ContentTypes();
		
		return hookSystem.filter(HOOK_REGISTER_CONTENT_TYPES, contentTypes).value();
	}
	
	public MediaForms mediaForms () {
		var mediaForms = new MediaForms();
		
		return hookSystem.filter(HOOK_REGISTER_MEDIA_FORMS, mediaForms).value();
	}
	
	public Menu menu() {
		var menu = new Menu();

		menu = hookSystem.filter(HOOK_MENU, menu).value();
		
		return menu;
	}
	
	public Map<String, Map<String, String>> translations () {
		Map<String, Map<String, String>> translations = new HashMap<>(Map.of(
				"de", new HashMap<>(),
				"en", new HashMap<>()
		));
		return hookSystem.filter(HOOK_TRANSLATIONS, translations).value();
	}
}
