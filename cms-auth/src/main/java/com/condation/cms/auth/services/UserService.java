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
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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
	
	public void addUser (Realm realm, String username, String password, String [] groups) throws IOException {
		var users = loadUsers(realm);
		users = new ArrayList<>(users.stream().filter(user -> !user.username.equals(username)).toList());
		users.add(new User(username, SecurityUtil.hash(password), groups));
		saveUsers(realm, users);
	}
	
	public void removeUser (Realm realm, String username) throws IOException {
		var users = loadUsers(realm);
		users = new ArrayList<>(users.stream().filter(user -> !user.username.equals(username)).toList());
		saveUsers(realm, users);
	}
	
	private static User fromString(final String userString) {
		List<String> userParts = userSplitter.splitToList(userString);

		var username = userParts.get(0);
		var passwordHash = userParts.get(1);
		var groups = Iterables.toArray(groupSplitter.split(userParts.get(2)), String.class);

		return new User(username, passwordHash, groups);
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
			final String hashedPassword = SecurityUtil.hash(password);
			
			var userOpt = loadUsers(realm).stream().filter(user -> user.username().equals(username)).findFirst();
			if (
					userOpt.isPresent()
					&& userOpt.get().passwordHash.equals(hashedPassword)) {
				return userOpt;
			}
			
			return Optional.empty();
		} catch (Exception ex) {
			log.error("", ex);
		}
		return Optional.empty();
	}
	
	
	private void saveUsers(Realm realm, List<User> users) throws IOException {
		Path usersFile = hostBase.resolve("config/" + FILENAME_PATTERN.formatted(realm.name));
		Files.deleteIfExists(usersFile);
		
		StringBuilder userContent = new StringBuilder();
		users.forEach(user -> userContent.append(user.line()));
		
		Files.writeString(usersFile, "# users file", StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		Files.writeString(usersFile, userContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}
	
	public static record User (String username, String passwordHash, String[] groups) {
	
		public String line () {
			return "%s:%s:%s\r\n".formatted(
					username,
					passwordHash,
					groups!= null ? String.join(",", groups) : ""
			);
		}
	}
	
	public static record Realm (String name) {
		public static Realm of (String name) {
			return new Realm(name);
		}
	}
}
