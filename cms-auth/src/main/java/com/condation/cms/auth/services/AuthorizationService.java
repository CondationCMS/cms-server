package com.condation.cms.auth.services;

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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.auth.permissions.Permission;
import com.condation.cms.auth.permissions.PermissionRegistry;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authorization service for role and permission checks.
 *
 * Permissions are managed via a central registry.
 * Roles only contain permission keys so that modules can add custom permissions.
 */
public class AuthorizationService {

    /**
     * Predefined roles with associated permission keys.
     * These can later be extended via configuration or by modules.
     */
    public enum Role {
        EDITOR(Set.of(Permissions.CONTENT_EDIT)),
        MANAGER(Set.of(Permissions.CONTENT_EDIT, Permissions.CACHE_INVALIDATE)),
        ADMIN(Set.of(Permissions.CONTENT_EDIT, Permissions.CACHE_INVALIDATE));

        private final Set<String> permissionKeys;

        Role(Set<String> permissionKeys) {
            this.permissionKeys = permissionKeys;
        }

        public Set<String> getPermissionKeys() {
            return permissionKeys;
        }

        public static Role fromString(String role) {
            try {
                return Role.valueOf(role.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null; // ignore unknown roles
            }
        }
    }

    /**
     * Collects all permission keys of a given user (based on their roles).
     */
    public Set<String> getPermissionKeys(User user) {
        if (user == null || user.roles() == null) {
            return Set.of();
        }
        return Arrays.stream(user.roles())
                .map(Role::fromString)
                .filter(Objects::nonNull)
                .flatMap(r -> r.getPermissionKeys().stream())
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

    // --- Register default/core permissions ---
    static {
        PermissionRegistry.register(Permission.CONTENT_EDIT);
    }
}
