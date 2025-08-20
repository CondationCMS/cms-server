package com.condation.cms.modules.ui.services;

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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.auth.permissions.Permission;
import com.condation.cms.auth.permissions.PermissionRegistry;
import com.condation.cms.auth.services.User;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

class RemoteMethodServiceTest {

	@BeforeAll
	public static void setup () {
		PermissionRegistry.register(new Permission("no", "test permission"));
	}
	
    @Test
    void shouldThrowRemoteMethodException_whenUserHasNoAccess() {
        // given
        RemoteMethod remoteMethod = new RemoteMethod() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RemoteMethod.class;
            }

            @Override
            public String name() {
                return "test";
            }

            @Override
            public String[] permissions() {
                return new String[]{"no"};
            }
        };

        RemoteMethodService.RMethod rMethod = new RemoteMethodService.RMethod(
                remoteMethod,
                parameters -> "should not be called"
        );

        User user = new User("testUser", "hash", new String[]{"user"}, Collections.emptyMap());

        // when / then
        assertThatThrownBy(() -> rMethod.execute(Map.of(), user))
                .isInstanceOf(RemoteMethodService.RemoteMethodException.class)
                .hasMessage("access not allowed");
    }

    @Test
    void shouldExecuteMethod_whenUserHasAccess() {
        // given
        RemoteMethod remoteMethod = new RemoteMethod() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RemoteMethod.class;
            }

            @Override
            public String name() {
                return "test";
            }

            @Override
            public String[] permissions() {
                return new String[]{Permissions.CONTENT_EDIT};
            }
        };

        RemoteMethodService.RMethod rMethod = new RemoteMethodService.RMethod(
                remoteMethod,
                parameters -> "success"
        );

        User user = new User("testUser", "hash", new String []{"editor"}, Collections.emptyMap()); // passende Rolle

        // when
        Object result = rMethod.execute(Map.of(), user);

        // then
        assertThat(result).isEqualTo("success");
    }
}
