package com.condation.cms.auth.services;

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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authorization service for role and permission checks.
 *
 * Permissions are managed via a central registry.
 * Roles only contain permission keys so that modules can add custom permissions.
 */
public class AuthorizationService {
	private final RoleService roleService;

	/** Uses the built-in roles; primarily useful for isolated consumers and tests. */
	public AuthorizationService() {
		this.roleService = null;
	}

	public AuthorizationService(RoleService roleService) {
		this.roleService = Objects.requireNonNull(roleService);
	}

    /**
     * Collects all permission keys of a given user (based on their roles).
     */
    public Set<String> getPermissionKeys(User user) {
        if (user == null || user.roles() == null) {
            return Set.of();
        }
		Map<String, Role> roles = rolesById();
		return Arrays.stream(user.roles())
				.map(role -> roles.get(role.toLowerCase(Locale.ROOT)))
				.filter(Objects::nonNull)
				.flatMap(role -> role.permissions().stream())
				.collect(Collectors.toSet());
    }

    /**
     * Checks if a user has a specific permission.
	 * @param user
	 * @param permissionKey
	 * @return 
     */
    public boolean hasPermission(User user, String permissionKey) {
        return getPermissionKeys(user).contains(permissionKey);
    }

    /**
     * Checks if a user has at least one of the given permissions.
	 * @param user
	 * @param required
	 * @return 
     */
    public boolean hasAnyPermission(User user, String... required) {
        Set<String> userPerms = getPermissionKeys(user);
        for (String key : required) {
            if (userPerms.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a user has all of the given permissions.
     */
    public boolean hasAllPermissions(User user, String... required) {
        Set<String> userPerms = getPermissionKeys(user);
        return userPerms.containsAll(Set.of(required));
    }

	private Map<String, Role> rolesById() {
		try {
			List<Role> roles = roleService == null ? RoleService.defaults() : roleService.list();
			return roles.stream().collect(Collectors.toMap(Role::id, role -> role));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not load roles", exception);
		}
	}
}
