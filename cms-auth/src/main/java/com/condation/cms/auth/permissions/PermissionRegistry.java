package com.condation.cms.auth.permissions;

/*-
 * #%L
 * cms-auth
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class PermissionRegistry {

	private static final Map<String, Permission> registry = new HashMap<>();

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
