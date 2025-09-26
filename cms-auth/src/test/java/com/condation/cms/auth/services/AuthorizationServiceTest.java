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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationServiceTest {

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }

    @Test
    void editorShouldHaveEditPermissionButNotPublishPermission() {
        User editor = new User("editor", "hash", new String[]{"editor"});

        assertThat(authorizationService.hasPermission(editor, Permissions.CONTENT_EDIT)).isTrue();
		assertThat(authorizationService.hasPermission(editor, Permissions.CACHE_INVALIDATE)).isFalse();
    }

    @Test
    void managerShouldHaveEditAndPublishPermissions() {
        User manager = new User("manager", "hash", new String[]{"manager"});

        assertThat(authorizationService.hasPermission(manager, Permissions.CONTENT_EDIT)).isTrue();
        assertThat(authorizationService.hasPermission(manager, Permissions.CACHE_INVALIDATE)).isTrue();
    }

    @Test
    void adminShouldHaveAllCorePermissions() {
        User admin = new User("admin", "hash", new String[]{"admin"});

        assertThat(authorizationService.hasPermission(admin, Permissions.CONTENT_EDIT)).isTrue();
        assertThat(authorizationService.hasPermission(admin, Permissions.CACHE_INVALIDATE)).isTrue();
    }

    @Test
    void hasAnyPermissionShouldWorkCorrectly() {
        User manager = new User("manager", "hash", new String[]{"manager"});

        assertThat(authorizationService.hasAnyPermission(manager, Permissions.CACHE_INVALIDATE, Permissions.CONTENT_EDIT)).isTrue();
        assertThat(authorizationService.hasAllPermissions(manager, Permissions.CACHE_INVALIDATE, "user.manage")).isFalse();
    }

    @Test
    void hasAllPermissionsShouldWorkCorrectly() {
        User admin = new User("admin", "hash", new String[]{"admin"});

        assertThat(authorizationService.hasAllPermissions(admin, Permissions.CACHE_INVALIDATE, Permissions.CONTENT_EDIT)).isTrue();
        assertThat(authorizationService.hasAllPermissions(admin, Permissions.CONTENT_EDIT, "unknown.permission")).isFalse();
    }

    @Test
    void customModulePermissionShouldBeRegisterable() {
        // Register a custom permission from a module
        PermissionRegistry.register(
                new Permission("blog.write", "Write blog posts")
        );

        // Extend role manually (ADMIN should conceptually have all, but for test we check via key set)
        User admin = new User("admin", "hash", new String[]{"admin"});

        assertThat(PermissionRegistry.get("blog.write")).isNotNull();
        // Admin does not automatically get blog.write unless Role.ADMIN is extended.
        assertThat(authorizationService.hasPermission(admin, "blog.write")).isFalse();
    }
}
