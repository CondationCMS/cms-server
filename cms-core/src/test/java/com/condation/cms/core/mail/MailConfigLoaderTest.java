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
import com.condation.cms.core.configuration.EnvironmentVariables;
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

		System.setProperty("cms.home", tempDir.toAbsolutePath().toString());

		// Create .env file for tests
		Path envFile = tempDir.resolve(".env");
		Files.writeString(envFile, """
								MAIL_DOMAIN=example.com
								SMTP_HOST=example.com
								SMTP_PORT=1234
								SMTP_USERNAME=smtp-user
                                SMTP_PASSWORD=smtp-password
                                MAILMARKET_HOST=marketing-host
                                MAILMARKET_USER=marketing-user
                                MAILMARKET_PASS=marketing-pass
								   """);

		// Arrange
		String yaml = """
			accounts:
              default:
                fromMail: "noreply@${env:MAIL_DOMAIN}"
                host: "${env:SMTP_HOST}"
                port: "${env:SMTP_PORT}"
                username: "${env:SMTP_USERNAME}"
                password: "${env:SMTP_PASSWORD}"
                
              marketing:
                fromMail: "marketingAccount@${env:MAIL_DOMAIN}"
                host: "${env:MAILMARKET_HOST}"
                port: "587"
                username: "${env:MAILMARKET_USER}"
                password: "${env:MAILMARKET_PASS}"
			""";
		Path configFile = createTempYamlFile(yaml);

		// Act
		MailConfig config = MailConfigLoader.load(configFile, new EnvironmentVariables());

		// Assert
		assertThat(config).isNotNull();
		assertThat(config.getAccounts()).hasSize(2);
		
		assertThat(config.getAccount("default")).isPresent();
		final MailConfig.Account defaulAccount = config.getAccount("default").get();
		assertThat(defaulAccount.getName()).isEqualTo("default");
		assertThat(defaulAccount.getHost()).isEqualTo("example.com");
		assertThat(defaulAccount.getPort()).isEqualTo(1234);
		assertThat(defaulAccount.getUsername()).isEqualTo("smtp-user");
		assertThat(defaulAccount.getPassword()).isEqualTo("smtp-password");
		assertThat(defaulAccount.getFromMail()).isEqualTo("noreply@example.com");
		
		assertThat(config.getAccount("marketing")).isPresent();
		final MailConfig.Account marketingAccount = config.getAccount("marketing").get();
		assertThat(marketingAccount.getName()).isEqualTo("marketing");
		assertThat(marketingAccount.getHost()).isEqualTo("marketing-host");
		assertThat(marketingAccount.getPort()).isEqualTo(587);
		assertThat(marketingAccount.getUsername()).isEqualTo("marketing-user");
		assertThat(marketingAccount.getPassword()).isEqualTo("marketing-pass");
		assertThat(marketingAccount.getFromMail()).isEqualTo("marketingAccount@example.com");
	}

	// Helper Methode
	private Path createTempYamlFile(String yamlContent) throws IOException {
		Path file = tempDir.resolve("config.yaml");
		Files.write(file, yamlContent.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
