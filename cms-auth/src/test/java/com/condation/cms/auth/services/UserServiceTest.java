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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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
	public void test_login_and_remove() throws Exception {
		
		com.condation.cms.auth.services.Realm realm = Realm.of("users");
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isEmpty();
		
		userService.addUser(realm, "test", "demo", new String[]{"eins","zwei"});
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isPresent();
		
		userService.removeUser(realm, "test");
		
		Assertions.assertThat(userService.login(realm, "test", "demo")).isEmpty();
	}
	
	@Test
	public void test_multiple_users() throws Exception {

		com.condation.cms.auth.services.Realm realm = Realm.of("musers");

		userService.addUser(realm, "test1", "demo", new String[]{"eins","zwei"});
		userService.addUser(realm, "test2", "demo", new String[]{"eins","zwei"});
		Assertions.assertThat(userService.login(realm,  "test1", "demo")).isPresent();
		Assertions.assertThat(userService.login(realm, "test1", "demo")).isPresent();
	}

	@Test
	public void addUser_rejectsUsernameThatWouldCorruptTheRealmFile() {
		var realm = Realm.of("invalid-usernames");

		Assertions.assertThatThrownBy(() -> userService.addUser(realm, "evil:admin", "demo", new String[]{"eins"}))
				.isInstanceOf(IllegalArgumentException.class);
		Assertions.assertThatThrownBy(() -> userService.addUser(realm, "evil\r\nnewline:hash:admin", "demo", new String[]{"eins"}))
				.isInstanceOf(IllegalArgumentException.class);
		Assertions.assertThatThrownBy(() -> userService.addUser(realm, "", "demo", new String[]{"eins"}))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void addUser_rejectsInvalidMailAddress() {
		var realm = Realm.of("invalid-mail");

		Assertions.assertThatThrownBy(() -> userService.addUser(realm, "mailuser", "demo", new String[]{"eins"},
				Map.of("mail", "not-an-email")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void addUser_acceptsValidMailAddress() throws Exception {
		var realm = Realm.of("valid-mail");

		userService.addUser(realm, "mailuser2", "demo", new String[]{"eins"}, Map.of("mail", "someone@example.com"));

		Assertions.assertThat(userService.byUsername(realm, "mailuser2"))
				.get()
				.satisfies(user -> Assertions.assertThat(user.data()).containsEntry("mail", "someone@example.com"));
	}

	@Test
	public void updateUser_rejectsInvalidMailAddress() throws Exception {
		var realm = Realm.of("update-invalid-mail");
		userService.addUser(realm, "mailuser3", "demo", new String[]{"eins"});

		Assertions.assertThatThrownBy(() -> userService.updateUser(realm, "mailuser3", null, new String[]{"eins"},
				Map.of("mail", "not-an-email")))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
