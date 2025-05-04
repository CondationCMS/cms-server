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

import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.ui.action.MenuHookAction;
import com.condation.cms.api.ui.action.MenuAction;
import com.condation.cms.api.ui.action.MenuScriptAction;
import com.condation.cms.api.ui.extensions.UIMenuExtensionPoint;
import com.condation.cms.api.ui.menu.Menu;
import com.condation.cms.api.ui.menu.MenuEntry;
import com.condation.modules.api.ModuleManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@RequiredArgsConstructor
public class MenuFactory {

	private final HookSystem hookSystem;
	private final ModuleManager moduleManager;

	public Menu createMenu() {
		UIHooks uiHooks = new UIHooks(hookSystem);
		var menu = uiHooks.menu();

		moduleManager.extensions(UIMenuExtensionPoint.class).forEach(extension -> {
			extension.addMenuItems(menu);
		});
		
		List<EntryHolder> entries = new ArrayList<>();
		moduleManager.extensions(UIMenuExtensionPoint.class).forEach(extension -> {
			entries.addAll(scan(extension));
		});
		
		insertEntriesIntoMenu(menu, entries);

		return menu;
	}

	private List<EntryHolder> scan(Object moduleInstance) {

		List<EntryHolder> entries = new ArrayList<>();
		
		for (Method method : moduleInstance.getClass().getDeclaredMethods()) {
			var menuAnn = method.getAnnotation(com.condation.cms.api.ui.annotations.MenuEntry.class);
			if (menuAnn == null) {
				continue;
			}

			method.setAccessible(true);
			MenuAction menuAction = null;

			// 1. Methode hat @Action?
			Action actionAnn = method.getAnnotation(Action.class);
			if (actionAnn != null) {
				menuAction = new MenuHookAction(actionAnn.value(), Map.of());
			} // 2. @Hook in @MenuEntry
			else if (!menuAnn.hookAction().value().isEmpty()) {
				menuAction = new MenuHookAction(menuAnn.hookAction().value(), Map.of());
			} // 3. @ScriptAction in @MenuEntry
			else if (!menuAnn.scriptAction().module().isEmpty()) {
				menuAction = new MenuScriptAction(menuAnn.scriptAction().module(), menuAnn.scriptAction().function(), Map.of());
			}

			var entry = MenuEntry.builder()
					.id(menuAnn.id())
					.name(menuAnn.name())
					.divider(menuAnn.divider())
					.position(menuAnn.position())
					.action(menuAction)
					.children(new ArrayList<MenuEntry>())
					.build();

			entries.add(new EntryHolder(menuAnn.parent(), entry));
		}
		
		return entries;
	}
	
	private void insertEntriesIntoMenu(Menu menu, List<EntryHolder> entries) {
		Map<String, MenuEntry> index = new HashMap<>();
		entries.forEach(holder -> index.put(holder.entry().getId(), holder.entry()));

		// füge alle mit parent == "" oder null in die Wurzel ein
		for (EntryHolder holder : entries) {
			String parentId = holder.parent();
			MenuEntry entry = holder.entry();

			if (parentId == null || parentId.isBlank()) {
				menu.addMenuEntry(entry);
			} else {
				// Versuche Parent in fertigem Menü zu finden
				Optional<MenuEntry> parentInMenu = findEntryById(menu, parentId);

				if (parentInMenu.isEmpty()) {
					// Versuche in den noch nicht eingefügten Entries
					MenuEntry parentInBatch = index.get(parentId);
					if (parentInBatch != null) {
						parentInBatch.getChildren().add(entry);
					} else {
						log.warn("Parent entry with ID '" + parentId + "' not found for menu entry '" + entry.getId() + "'");
					}
				} else {
					parentInMenu.get().addChildren(entry);
				}
			}
		}

		// Jetzt alle "Wurzel"-Einträge, die nicht direkt im Menü sind, einfügen
		for (EntryHolder holder : entries) {
			String parentId = holder.parent();
			if (parentId == null || parentId.isBlank()) {
				menu.addMenuEntry(holder.entry());
			}
		}
	}

	public Optional<MenuEntry> findEntryById(Menu menu, String id) {
		if (menu.entries() == null) {
			return Optional.empty();
		}
		for (MenuEntry entry : menu.entries()) {
			Optional<MenuEntry> result = findEntryByIdRecursive(entry, id);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}

	private Optional<MenuEntry> findEntryByIdRecursive(MenuEntry entry, String id) {
		if (entry.getId().equals(id)) {
			return Optional.of(entry);
		}
		if (entry.getChildren() == null) {
			return Optional.empty();
		}
		for (MenuEntry child : entry.getChildren()) {
			Optional<MenuEntry> result = findEntryByIdRecursive(child, id);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}
	
	private record EntryHolder (String parent, MenuEntry entry) {}
}
