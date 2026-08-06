package com.condation.cms.cli.commands.server;

/*-
 * #%L
 * CMS Server
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

import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.UserService;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/**
 * Exercises the {@code add_user} CLI command to confirm the same
 * username/mail validation that protects the manager RPC endpoints also
 * applies when a server operator adds a user from the command line.
 */
class AddUserTest {

	@TempDir
	Path serverHome;

	private String previousServerHome;

	@BeforeEach
	void configureServerHome() {
		previousServerHome = System.getProperty("cms.home");
		System.setProperty("cms.home", serverHome.toString());
	}

	@AfterEach
	void restoreServerHome() {
		if (previousServerHome == null) {
			System.clearProperty("cms.home");
		} else {
			System.setProperty("cms.home", previousServerHome);
		}
	}

	@Test
	void addUser_rejectsUsernameThatWouldCorruptTheRealmFile() {
		var exitCode = new CommandLine(new AddUser()).execute("evil:admin", "secret123", "someone@example.com");

		Assertions.assertThat(exitCode).isNotZero();
	}

	@Test
	void addUser_rejectsInvalidMailAddress() {
		var exitCode = new CommandLine(new AddUser()).execute("validname", "secret123", "not-an-email");

		Assertions.assertThat(exitCode).isNotZero();
	}

	@Test
	void addUser_acceptsValidUsernameAndMail() throws Exception {
		var exitCode = new CommandLine(new AddUser()).execute("validname", "secret123", "someone@example.com");

		Assertions.assertThat(exitCode).isZero();
		Assertions.assertThat(new UserService(serverHome).byUsername(Realm.of("users"), "validname"))
				.get()
				.satisfies(user -> Assertions.assertThat(user.data()).containsEntry("mail", "someone@example.com"));
	}
}
