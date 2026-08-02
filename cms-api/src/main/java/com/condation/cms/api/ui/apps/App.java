package com.condation.cms.api.ui.apps;

/*-
 * #%L
 * CMS Api
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ui.action.UIAction;
import java.util.List;
import java.util.Objects;

/**
 * An application available from the manager app launcher.
 *
 * @param id stable app identifier
 * @param title title displayed in the app launcher
 * @param icon URL of the app icon
 * @param action action executed when the app is opened
 * @param permissions permissions required to see the app
 */
public record App(
		String id,
		String title,
		String icon,
		UIAction action,
		List<String> permissions) {

	public App {
		id = requireText(id, "App id");
		title = requireText(title, "App title");
		icon = requireText(icon, "App icon");
		action = Objects.requireNonNull(action, "App action is required");
		permissions = permissions == null ? List.of() : List.copyOf(permissions);
	}

	public App(String id, String title, String icon, UIAction action) {
		this(id, title, icon, action, List.of());
	}

	private static String requireText(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value.trim();
	}
}
