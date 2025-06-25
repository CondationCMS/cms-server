package com.condation.cms.auth.services;

/*-
 * #%L
 * cms-auth
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import com.condation.cms.auth.utils.SecurityUtil;
import com.condation.cms.core.configuration.GSONProvider;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class UserService {

	private static final String FILENAME_PATTERN = "%s.realm";

	private final static Splitter userSplitter = Splitter.on(":").trimResults();
	private final static Splitter groupSplitter = Splitter.on(",").trimResults();

	private final Path hostBase;

	public void addUser(Realm realm, String username, String password, String[] roles) throws IOException {
		List<User> users = loadUsers(realm);
		users = new ArrayList<>(users.stream()
				.filter(user -> !user.username().equals(username))
				.toList());

		byte[] salt = SecurityUtil.generateSalt();
		String saltBase64 = Base64.getEncoder().encodeToString(salt);
		String passwordHash = SecurityUtil.hashPBKDF2(password, salt);

		Map<String, Object> data = new HashMap<>();
		data.put("salt", saltBase64);

		users.add(new User(username, passwordHash, roles, data));
		saveUsers(realm, users);
	}

	public void removeUser(Realm realm, String username) throws IOException {
		var users = loadUsers(realm);
		users = new ArrayList<>(users.stream().filter(user -> !user.username.equals(username)).toList());
		saveUsers(realm, users);
	}

	public Optional<User> byUsername(final Realm realm, final String username) {
		try {
			return loadUsers(realm).stream().filter(user -> user.username().equals(username)).findFirst();
		} catch (Exception ex) {
			log.error("", ex);
		}
		return Optional.empty();
	}

	private static User fromString(final String userString) {
		List<String> userParts = userSplitter.splitToList(userString);

		String username = userParts.get(0);
		String passwordHash = userParts.get(1);
		String[] groups = Iterables.toArray(groupSplitter.split(userParts.get(2)), String.class);
		Map<String, Object> data = new HashMap<>();

		if (userParts.size() >= 4) {
			try {
				String json = new String(Base64.getDecoder().decode(userParts.get(3)), StandardCharsets.UTF_8);
				data = GSONProvider.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
				}.getType());
			} catch (Exception e) {
				log.warn("Konnte Nutzerdaten nicht lesen für {}", username, e);
			}
		}

		return new User(username, passwordHash, groups, data);
	}

	private List<User> loadUsers(final Realm realm) throws IOException {
		Path usersFile = hostBase.resolve("config/" + FILENAME_PATTERN.formatted(realm.name));
		List<User> users = new ArrayList<>();
		if (Files.exists(usersFile)) {
			List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);

			for (String line : lines) {
				if (!line.startsWith("#")) {
					try {
						users.add(fromString(line));
					} catch (Exception e) {
						log.error("error loading user", e);
					}
				}
			}
		}

		return users;
	}

	public Optional<User> login(final Realm realm, final String username, final String password) {
		try {
			var userOpt = loadUsers(realm).stream()
					.filter(user -> user.username().equals(username))
					.findFirst();

			if (userOpt.isEmpty()) {
				return Optional.empty();
			}

			User user = userOpt.get();
			Map<String, Object> data = user.data();

			if (data == null || !data.containsKey("salt")) {
				log.warn("Benutzer '{}' hat keinen Salt gespeichert", username);
				return Optional.empty();
			}

			String base64Salt = String.valueOf(data.get("salt"));
			byte[] salt = Base64.getDecoder().decode(base64Salt);

			boolean valid = SecurityUtil.verifyPassword(password, user.passwordHash(), salt);

			return valid ? Optional.of(user) : Optional.empty();

		} catch (Exception ex) {
			log.error("Fehler beim Login von Benutzer '{}'", username, ex);
			return Optional.empty();
		}
	}

	private void saveUsers(Realm realm, List<User> users) throws IOException {
		Path usersFile = hostBase.resolve("config/" + FILENAME_PATTERN.formatted(realm.name));
		Files.deleteIfExists(usersFile);

		StringBuilder userContent = new StringBuilder();
		users.forEach(user -> userContent.append(user.line()));

		Files.writeString(usersFile, "# users file", StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		Files.writeString(usersFile, userContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}

	public static record User(String username, String passwordHash, String[] roles, Map<String, Object> data) {

		public User(String username, String passwordHash, String[] roles) {
			this(username, passwordHash, roles, Collections.emptyMap());
		}
		public String line() {
			try {
				String json = GSONProvider.GSON.toJson(data != null ? data : Map.of());
				String encodedData = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
				return "%s:%s:%s:%s\r\n".formatted(
						username,
						passwordHash,
						roles != null ? String.join(",", roles) : "",
						encodedData
				);
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
			return Objects.equals(username, other.username)
					&& Objects.equals(passwordHash, other.passwordHash)
					&& Arrays.equals(groups, other.groups)
					&& Objects.equals(data, other.data);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(username, passwordHash, data);
			result = 31 * result + Arrays.hashCode(groups);
			return result;
		}
	}

	public static record Realm(String name) {

		public static Realm of(String name) {
			return new Realm(name);
		}
	}
}
