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
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.auth.permissions.Permission;
import com.condation.cms.auth.permissions.PermissionRegistry;
import com.condation.cms.auth.services.AuthorizationService;
import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.Role;
import com.condation.cms.auth.services.RoleService;
import com.condation.cms.auth.services.User;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** RPC API used by the manager's role and user applications. */
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteAccessManagementEndpoints extends AbstractRemoteMethodeExtension {
	private static final Realm MANAGER_REALM = Realm.of("manager-users");

	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	private static final String ROLES = "roles";
	private static final String MAIL = "mail";
	
	@RemoteMethod(name = "access.permissions.list", permissions = {Permissions.ROLE_MANAGE})
	public Object permissions(Map<String, Object> parameters) {
		return PermissionRegistry.all().stream()
				.sorted(Comparator.comparing(Permission::key))
				.toList();
	}

	@RemoteMethod(name = "access.roles.list", permissions = {Permissions.ROLE_MANAGE, Permissions.USER_MANAGE})
	public Object roles(Map<String, Object> parameters) throws RPCException {
		try {
			return roleService().list();
		} catch (Exception exception) {
			throw error("Could not load roles", exception);
		}
	}

	@RemoteMethod(name = "access.roles.save", permissions = {Permissions.ROLE_MANAGE})
	public Object saveRole(Map<String, Object> parameters) throws RPCException {
		try {
			Role role = convert(parameters.get("role"), Role.class, "Role is required");
			Set<String> knownPermissions = PermissionRegistry.all().stream()
					.map(Permission::key).collect(Collectors.toSet());
			if (!knownPermissions.containsAll(role.permissions())) {
				throw new RPCException(1, "Role contains unknown permissions");
			}
			return roleService().save(role);
		} catch (RPCException exception) {
			throw exception;
		} catch (Exception exception) {
			throw error("Could not save role", exception);
		}
	}

	@RemoteMethod(name = "access.roles.delete", permissions = {Permissions.ROLE_MANAGE})
	public Object deleteRole(Map<String, Object> parameters) throws RPCException {
		String id = string(parameters, "id");
		try {
			boolean assigned = userService().listUsers(MANAGER_REALM).stream()
					.anyMatch(user -> user.roles() != null && Arrays.asList(user.roles()).contains(id));
			if (assigned) {
				throw new RPCException(1, "Role is still assigned to one or more users");
			}
			return Map.of("deleted", roleService().delete(id));
		} catch (RPCException exception) {
			throw exception;
		} catch (Exception exception) {
			throw error("Could not delete role", exception);
		}
	}

	@RemoteMethod(name = "access.users.list", permissions = {Permissions.USER_MANAGE})
	public Object users(Map<String, Object> parameters) throws RPCException {
		try {
			return userService().listUsers(MANAGER_REALM).stream().map(this::userDto).toList();
		} catch (Exception exception) {
			throw error("Could not load users", exception);
		}
	}

	@RemoteMethod(name = "access.users.create", permissions = {Permissions.USER_MANAGE})
	public Object createUser(Map<String, Object> parameters) throws RPCException {
		String username = string(parameters, USERNAME);
		String password = string(parameters, PASSWORD);
		try {
			if (userService().byUsername(MANAGER_REALM, username).isPresent()) {
				throw new RPCException(1, "User already exists: " + username);
			}
			String[] roles = roleIds(parameters);
			validateRoles(roles);
			ensureCanAssignRoles(roles);
			userService().addUser(MANAGER_REALM, username, password, roles, userData(parameters));
			return userDto(userService().byUsername(MANAGER_REALM, username).orElseThrow());
		} catch (RPCException exception) {
			throw exception;
		} catch (Exception exception) {
			throw error("Could not create user", exception);
		}
	}
	

	@RemoteMethod(name = "access.users.update", permissions = {Permissions.USER_MANAGE})
	public Object updateUser(Map<String, Object> parameters) throws RPCException {
		String username = string(parameters, USERNAME);
		try {
			String[] roles = roleIds(parameters);
			validateRoles(roles);
			ensureCanAssignRoles(roles);
			String password = parameters.get(PASSWORD) instanceof String value ? value : null;
			userService().updateUser(MANAGER_REALM, username, password, roles, userData(parameters));
			return userDto(userService().byUsername(MANAGER_REALM, username).orElseThrow());
		} catch (RPCException exception) {
			throw exception;
		} catch (Exception exception) {
			throw error("Could not update user", exception);
		}
	}

	@RemoteMethod(name = "access.users.delete", permissions = {Permissions.USER_MANAGE})
	public Object deleteUser(Map<String, Object> parameters) throws RPCException {
		String username = string(parameters, USERNAME);
		if (username.equals(getUserName())) {
			throw new RPCException(1, "You cannot delete your own account");
		}
		try {
			boolean exists = userService().byUsername(MANAGER_REALM, username).isPresent();
			if (exists) userService().removeUser(MANAGER_REALM, username);
			return Map.of("deleted", exists);
		} catch (Exception exception) {
			throw error("Could not delete user", exception);
		}
	}

	private void validateRoles(String[] roleIds) throws RPCException, IOException {
		Set<String> known = roleService().list().stream().map(Role::id).collect(Collectors.toSet());
		if (!known.containsAll(Arrays.asList(roleIds))) {
			throw new RPCException(1, "User contains unknown roles");
		}
	}

	/**
	 * A caller without {@code ROLE_MANAGE} may only grant roles whose permissions
	 * are a subset of their own; otherwise they could bootstrap themselves or
	 * others into an administrative role through {@code USER_MANAGE} alone.
	 */
	private void ensureCanAssignRoles(String[] roleIds) throws RPCException, IOException {
		User caller = currentUser().orElseThrow(() -> new RPCException(403, "not authenticated"));
		if (authorizationService().hasPermission(caller, Permissions.ROLE_MANAGE)) {
			return;
		}
		Map<String, Role> rolesById = roleService().list().stream()
				.collect(Collectors.toMap(Role::id, role -> role));
		Set<String> requiredPermissions = Arrays.stream(roleIds)
				.map(rolesById::get)
				.filter(Objects::nonNull)
				.flatMap(role -> role.permissions().stream())
				.collect(Collectors.toSet());
		if (!authorizationService().hasAllPermissions(caller, requiredPermissions.toArray(String[]::new))) {
			throw new RPCException(403, "Cannot assign a role with permissions you do not have");
		}
	}

	private Optional<User> currentUser() {
		String username = getUserName();
		if (username.isBlank()) return Optional.empty();
		return userService().byUsername(MANAGER_REALM, username);
	}

	private AuthorizationService authorizationService() {
		return new AuthorizationService(roleService());
	}

	private String[] roleIds(Map<String, Object> parameters) throws RPCException {
		Object value = parameters.get(ROLES);
		if (!(value instanceof List<?> values) || values.isEmpty()) {
			throw new RPCException(1, "At least one role is required");
		}
		return values.stream().map(String::valueOf).toArray(String[]::new);
	}

	private Map<String, Object> userData(Map<String, Object> parameters) {
		Map<String, Object> data = new HashMap<>();
		if (parameters.get(MAIL) instanceof String mail && !mail.isBlank()) data.put(MAIL, mail.trim());
		return data;
	}

	private Map<String, Object> userDto(User user) {
		return Map.of(USERNAME, user.username(), 
				MAIL, user.data() != null ? String.valueOf(user.data().getOrDefault(MAIL, "")) : "", 
				ROLES, user.roles() == null ? List.of() : Arrays.asList(user.roles()));
	}

	private String string(Map<String, Object> parameters, String key) throws RPCException {
		if (!(parameters.get(key) instanceof String value) || value.isBlank()) {
			throw new RPCException(1, key + " is required");
		}
		return value.trim();
	}

	private <T> T convert(Object value, Class<T> type, String message) throws RPCException {
		if (value == null) throw new RPCException(1, message);
		try {
			return UIGsonProvider.INSTANCE.fromJson(UIGsonProvider.INSTANCE.toJson(value), type);
		} catch (RuntimeException _) {
			throw new RPCException(1, message);
		}
	}

	private UserService userService() {
		return getContext().get(InjectorFeature.class).injector().getInstance(UserService.class);
	}

	private RoleService roleService() {
		return getContext().get(InjectorFeature.class).injector().getInstance(RoleService.class);
	}

	private RPCException error(String message, Exception exception) {
		String detail = exception.getMessage();
		return new RPCException(1, detail == null || detail.isBlank() ? message : message + ": " + detail);
	}
}
