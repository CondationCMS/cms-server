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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.core.configuration.GSONProvider;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Persists manager roles in {@code config/manager-roles.json}. */
public class RoleService {
	private static final String FILENAME = "manager-roles.json";
	private static final String ID_PATTERN = "[a-z][a-z0-9_-]*";

	private final Path rolesFile;

	public RoleService(Path serverBase) {
		this.rolesFile = serverBase.resolve("config").resolve(FILENAME);
	}

	public synchronized List<Role> list() throws IOException {
		return load().values().stream()
				.sorted(Comparator.comparing(Role::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public synchronized Optional<Role> get(String id) throws IOException {
		return Optional.ofNullable(load().get(normalizeId(id)));
	}

	public synchronized Role save(Role role) throws IOException {
		validate(role);
		Map<String, Role> roles = load();
		roles.put(role.id(), role);
		write(roles);
		return role;
	}

	public synchronized boolean delete(String id) throws IOException {
		Map<String, Role> roles = load();
		boolean removed = roles.remove(normalizeId(id)) != null;
		if (removed) {
			write(roles);
		}
		return removed;
	}

	public static List<Role> defaults() {
		return List.of(
				new Role("editor", "Editor", Set.of(
						Permissions.CONTENT_EDIT,
						Permissions.WORKFLOW_EXECUTE)),
				new Role("manager", "Manager", Set.of(
						Permissions.CONTENT_EDIT,
						Permissions.CACHE_INVALIDATE,
						Permissions.MENU_MANAGE,
						Permissions.WORKFLOW_EXECUTE,
						Permissions.WORKFLOW_PUBLISH)),
				new Role("admin", "Administrator", Set.of(
						Permissions.CONTENT_EDIT,
						Permissions.CACHE_INVALIDATE,
						Permissions.MENU_MANAGE,
						Permissions.WORKFLOW_EXECUTE,
						Permissions.WORKFLOW_PUBLISH,
						Permissions.USER_MANAGE,
						Permissions.ROLE_MANAGE)));
	}

	private Map<String, Role> load() throws IOException {
		if (!Files.exists(rolesFile)) {
			return index(defaults());
		}
		String json = Files.readString(rolesFile, StandardCharsets.UTF_8);
		List<Role> roles = GSONProvider.GSON.fromJson(json, new TypeToken<List<Role>>() { }.getType());
		return index(roles == null ? List.of() : roles);
	}

	private Map<String, Role> index(List<Role> roles) {
		Map<String, Role> result = new LinkedHashMap<>();
		roles.forEach(role -> result.put(role.id(), role));
		return result;
	}

	private void write(Map<String, Role> roles) throws IOException {
		Files.createDirectories(rolesFile.getParent());
		Path temporaryFile = Files.createTempFile(rolesFile.getParent(), "manager-roles-", ".tmp");
		try {
			List<Role> sorted = new ArrayList<>(roles.values());
			sorted.sort(Comparator.comparing(Role::id));
			Files.writeString(temporaryFile, GSONProvider.GSON.toJson(sorted), StandardCharsets.UTF_8);
			try {
				Files.move(temporaryFile, rolesFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException _) {
				Files.move(temporaryFile, rolesFile, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporaryFile);
		}
	}

	private void validate(Role role) {
		if (role == null || !role.id().matches(ID_PATTERN)) {
			throw new IllegalArgumentException("Role id must match " + ID_PATTERN);
		}
	}

	private String normalizeId(String id) {
		return id == null ? "" : id.trim().toLowerCase();
	}
}
