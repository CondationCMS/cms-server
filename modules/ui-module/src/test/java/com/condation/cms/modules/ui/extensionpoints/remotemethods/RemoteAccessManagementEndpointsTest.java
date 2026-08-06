package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.Role;
import com.condation.cms.auth.services.RoleService;
import com.condation.cms.auth.services.UserService;
import com.google.inject.Guice;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the manager role/user RPC endpoints, in particular that assigning a
 * role can never grant more permissions than the caller already has unless
 * the caller holds {@code ROLE_MANAGE}.
 */
class RemoteAccessManagementEndpointsTest {

	private static final Realm MANAGER_REALM = Realm.of("manager-users");

	@TempDir
	Path serverBase;

	private UserService userService;
	private RoleService roleService;
	private RemoteAccessManagementEndpoints endpoints;

	@BeforeEach
	void setUp() throws Exception {
		userService = new UserService(serverBase);
		roleService = new RoleService(serverBase);

		// Persist the built-in roles plus two USER_MANAGE-only roles that a
		// real admin might create to delegate user administration.
		roleService.save(new Role("helper", "Helper", Set.of(Permissions.USER_MANAGE)));
		roleService.save(new Role("support", "Support", Set.of(Permissions.USER_MANAGE)));

		userService.addUser(MANAGER_REALM, "support", "secret123", new String[]{"support"});
		userService.addUser(MANAGER_REALM, "root", "secret123", new String[]{"admin"});

		var injector = Guice.createInjector(binder -> {
			binder.bind(UserService.class).toInstance(userService);
			binder.bind(RoleService.class).toInstance(roleService);
		});
		var context = new SiteModuleContext();
		context.add(InjectorFeature.class, new InjectorFeature(injector));

		endpoints = new RemoteAccessManagementEndpoints();
		endpoints.setContext(context);
	}

	private RequestContext contextFor(String username) {
		RequestContext requestContext = new RequestContext();
		requestContext.add(AuthFeature.class, new AuthFeature(username));
		return requestContext;
	}

	@Test
	void createUser_rejectsEscalation_whenCallerLacksRoleManage() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("support"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "victim",
						"password", "secret123",
						"roles", List.of("admin")))))
				.isInstanceOf(RPCException.class)
				.satisfies(ex -> assertThat(((RPCException) ex).getCode()).isEqualTo(403));
	}

	@Test
	void updateUser_rejectsSelfEscalation_whenCallerLacksRoleManage() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("support"))
				.call(() -> endpoints.updateUser(Map.of(
						"username", "support",
						"roles", List.of("admin")))))
				.isInstanceOf(RPCException.class)
				.satisfies(ex -> assertThat(((RPCException) ex).getCode()).isEqualTo(403));
	}

	@Test
	void createUser_allowsAssigningRoleWithinCallersOwnPermissions() throws RPCException {
		Object result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("support"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "newbie",
						"password", "secret123",
						"roles", List.of("helper"))));

		@SuppressWarnings("unchecked")
		Map<String, Object> dto = (Map<String, Object>) result;
		assertThat(dto).containsEntry("username", "newbie");
		assertThat(userService.byUsername(MANAGER_REALM, "newbie")).isPresent();
	}

	@Test
	void createUser_allowsAnyRole_whenCallerHasRoleManage() throws RPCException {
		Object result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("root"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "vip",
						"password", "secret123",
						"roles", List.of("admin"))));

		@SuppressWarnings("unchecked")
		Map<String, Object> dto = (Map<String, Object>) result;
		assertThat(dto).containsEntry("username", "vip");
	}

	@Test
	void createUser_rejectsUnknownRole() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("root"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "ghost",
						"password", "secret123",
						"roles", List.of("ghost-role")))))
				.isInstanceOf(RPCException.class)
				.satisfies(ex -> assertThat(((RPCException) ex).getCode()).isEqualTo(1));
	}

	@Test
	void deleteUser_rejectsDeletingOwnAccount() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("support"))
				.call(() -> endpoints.deleteUser(Map.of("username", "support"))))
				.isInstanceOf(RPCException.class)
				.satisfies(ex -> assertThat(((RPCException) ex).getCode()).isEqualTo(1));
	}

	@Test
	void createUser_rejectsUsernameThatWouldCorruptTheRealmFile() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("root"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "evil:admin",
						"password", "secret123",
						"roles", List.of("helper")))))
				.isInstanceOf(RPCException.class);
	}

	@Test
	void createUser_rejectsInvalidMailAddress() {
		assertThatThrownBy(() -> ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, contextFor("root"))
				.call(() -> endpoints.createUser(Map.of(
						"username", "mailtest",
						"password", "secret123",
						"roles", List.of("helper"),
						"mail", "not-an-email"))))
				.isInstanceOf(RPCException.class);
	}
}
