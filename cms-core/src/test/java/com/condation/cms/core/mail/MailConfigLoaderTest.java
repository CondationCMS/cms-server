package com.condation.cms.core.mail;

/*-
 * #%L
 * cms-core
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

@DisplayName("MailConfigLoader Tests")
class MailConfigLoaderTest {

	@TempDir
	Path tempDir;

	@Test
	@DisplayName("sollte valide YAML mit Accounts auf MailConfig mappen")
	void testLoadValidConfig() throws IOException {
		// Arrange
		String yaml = """
			accounts:
			  - host: smtp.example.com
			    port: 587
			    username: test@example.com
			    password: mypassword
			  - host: smtp.gmail.com
			    port: 465
			    username: user@gmail.com
			    password: gmailpass
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile);

		// Assert
		assertThat(config).isNotNull();
		assertThat(config.getAccounts())
			.hasSize(2)
			.extracting("host", "port", "username")
			.containsExactly(
				tuple("smtp.example.com", 587, "test@example.com"),
				tuple("smtp.gmail.com", 465, "user@gmail.com")
			);
	}

	@Test
	@DisplayName("sollte einzelnen Account korrekt mappen")
	void testLoadSingleAccount() throws IOException {
		// Arrange
		String yaml = """
			accounts:
			  - host: mail.server.de
			    port: 587
			    username: admin
			    password: secret123
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile);

		// Assert
		assertThat(config.getAccounts())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("host", "mail.server.de")
			.hasFieldOrPropertyWithValue("port", 587)
			.hasFieldOrPropertyWithValue("username", "admin")
			.hasFieldOrPropertyWithValue("password", "secret123");
	}

	@Test
	@DisplayName("sollte leere Accounts-Liste handhaben")
	void testLoadEmptyAccounts() throws IOException {
		// Arrange
		String yaml = """
			accounts: []
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile);

		// Assert
		assertThat(config.getAccounts()).isEmpty();
	}

	@Test
	@DisplayName("sollte MailConfig-Instanz zurückgeben")
	void testLoadReturnsMailConfigInstance() throws IOException {
		// Arrange
		String yaml = """
			accounts:
			  - host: localhost
			    port: 25
			    username: user
			    password: pass
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile);

		// Assert
		assertThat(config).isInstanceOf(MailConfig.class);
	}

	@Test
	@DisplayName("sollte IOException werfen wenn Datei nicht existiert")
	void testLoadNonExistentFile() {
		// Arrange
		Path nonExistentFile = Paths.get(tempDir.toString(), "not-exists.yaml");

		// Act & Assert
		assertThatThrownBy(() -> MailConfigLoader.load(nonExistentFile))
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("sollte mehrere Accounts mit unterschiedlichen Properties mappen")
	void testLoadMultipleAccountsWithDifferentValues() throws IOException {
		// Arrange
		String yaml = """
			accounts:
			  - host: smtp1.example.com
			    port: 587
			    username: user1@example.com
			    password: pass1
			  - host: smtp2.example.com
			    port: 465
			    username: user2@example.com
			    password: pass2
			  - host: smtp3.example.com
			    port: 25
			    username: user3@example.com
			    password: pass3
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile);

		// Assert
		assertThat(config.getAccounts())
			.hasSize(3)
			.extracting("host", "port")
			.containsExactly(
				tuple("smtp1.example.com", 587),
				tuple("smtp2.example.com", 465),
				tuple("smtp3.example.com", 25)
			);
	}

	// Helper Methode
	private Path createTempYamlFile(String yamlContent) throws IOException {
		Path file = tempDir.resolve("config.yaml");
		Files.write(file, yamlContent.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
