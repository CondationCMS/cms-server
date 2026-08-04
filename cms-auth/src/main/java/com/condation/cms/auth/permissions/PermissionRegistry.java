package com.condation.cms.auth.permissions;

/*-
 * #%L
 * CMS Auth
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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class PermissionRegistry {

	private static final Map<String, Permission> registry = new ConcurrentHashMap<>();

	static {
		register(Permission.CONTENT_EDIT);
		register(Permission.CACHE_INVALIDATE);
		register(Permission.USER_MANAGE);
		register(Permission.ROLE_MANAGE);
		register(Permission.MENU_MANAGE);
		register(Permission.WORKFLOW_EXECUTE);
		register(Permission.WORKFLOW_PUBLISH);
	}

	public static void register(Permission permission) {
		registry.put(permission.key(), permission);
	}

	public static Permission get(String key) {
		return registry.get(key);
	}

	public static Collection<Permission> all() {
		return registry.values();
	}
}
