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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class UserServiceTest {
	
	private static UserService userService;

	@BeforeAll
	public static void setup () throws IOException {
		var base = Path.of("target/" + System.currentTimeMillis());
		Files.createDirectories(base);
		var hostConfig = base.resolve("demo/config");
		Files.createDirectories(hostConfig);
		userService = new UserService(base.resolve("demo/"));
	}
	

	@Test
	public void test_login_and_remove() throws IOException {
		
		var realm = UserService.Realm.of("users");
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isEmpty();
		
		userService.addUser(realm, "test", "demo", new String[]{"eins","zwei"});
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isPresent();
		
		userService.removeUser(realm, "test");
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isEmpty();
	}
	
	@Test
	public void test_multiple_users() throws IOException {
		
		var realm = UserService.Realm.of("musers");
		
		userService.addUser(realm, "test1", "demo", new String[]{"eins","zwei"});
		userService.addUser(realm, "test2", "demo", new String[]{"eins","zwei"});
		Assertions.assertThat(userService.login(realm,  "test1", "demo")).isPresent();
		Assertions.assertThat(userService.login(realm, "test1", "demo")).isPresent();
	}
	
}
