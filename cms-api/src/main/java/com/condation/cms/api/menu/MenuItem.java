package com.condation.cms.api.menu;

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

import java.util.ArrayList;
import java.util.List;

/**
 * A single, potentially nested entry in a navigation menu.
 */
public record MenuItem(
		String id,
		String type,
		String label,
		String url,
		String target,
		boolean enabled,
		List<MenuItem> children,
		boolean current) {

	public MenuItem {
		children = children == null ? new ArrayList<>() : new ArrayList<>(children);
	}

	public MenuItem(
			String id,
			String type,
			String label,
			String url,
			String target,
			boolean enabled,
			List<MenuItem> children) {
		this(id, type, label, url, target, enabled, children, false);
	}
}
