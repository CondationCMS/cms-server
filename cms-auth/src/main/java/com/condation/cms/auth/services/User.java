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

import com.condation.cms.core.configuration.GSONProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;


/**
 *
 * @author thorstenmarx
 */
public record User (String username, String passwordHash, String [] roles, Map<String, Object> data) {

	public User(String username, String passwordHash, String[] roles) {
		this(username, passwordHash, roles, Collections.emptyMap());
	}

	public String line() {
		try {
			String json = GSONProvider.GSON.toJson(data != null ? data : Map.of());
			String encodedData = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			return "%s:%s:%s:%s\r\n".formatted(username, passwordHash, roles != null ? String.join(",", roles) : "", encodedData);
		} catch (Exception e) {
			throw new RuntimeException("Error writing user data", e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof User other)) {
			return false;
		}
		return Objects.equals(username, other.username) && Objects.equals(passwordHash, other.passwordHash) && Arrays.equals(roles, other.roles) && Objects.equals(data, other.data);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(username, passwordHash, data);
		result = 31 * result + Arrays.hashCode(roles);
		return result;
	}

	@Override
	public String toString() {
		return "User{" + "username='" + username + '\'' + ", passwordHash='***'" + ", roles=" + Arrays.toString(roles) + ", data=" + data + '}';
	}
}
