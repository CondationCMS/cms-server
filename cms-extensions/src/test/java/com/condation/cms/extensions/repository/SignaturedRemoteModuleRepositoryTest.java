package com.condation.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
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
import com.sun.net.httpserver.HttpServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignaturedRemoteModuleRepositoryTest {

	private HttpServer fileServer;

	private String fileBaseUrl;

	private String hash;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DummyExtensionInfo {

		String name;
		String version;
		String file;
		String sinature;
	}

	@BeforeAll
	void setupServers() throws Exception {
		fileServer = HttpServer.create(new InetSocketAddress(0), 0);

		int filePort = fileServer.getAddress().getPort();

		fileBaseUrl = "http://localhost:" + filePort + "/files";

		// Erstelle eine ZIP-Datei mit Dummy-Inhalt
		Path tempDir = Files.createTempDirectory("test-zip");
		Path contentFile = tempDir.resolve("content.txt");
		Files.writeString(contentFile, "test content");

		Path zipFile = Files.createTempFile("test", ".zip");
		try (var zip = new java.util.zip.ZipOutputStream(Files.newOutputStream(zipFile))) {
			// Verzeichnis explizit anlegen
			var dirEntry = new ZipEntry("test-module/");
			zip.putNextEntry(dirEntry);
			zip.closeEntry();

			// Datei im Verzeichnis ablegen
			var fileEntry = new ZipEntry("test-module/content.txt");
			zip.putNextEntry(fileEntry);
			zip.write("test content".getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}

		// Berechne den Hash der Datei
		hash = com.condation.cms.core.utils.HashVerifier.calculateSHA256(zipFile);

		// Statische URL für den Download
		fileServer.createContext("/files/test.zip", exchange -> {
			byte[] data = Files.readAllBytes(zipFile);
			exchange.sendResponseHeaders(200, data.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(data);
			}
		});

		fileServer.start();
	}

	@AfterAll
	void tearDown() {
		fileServer.stop(0);
	}

	@Test
	void shouldDownloadAndVerifyFileSignature() throws Exception {

		// Zielverzeichnis
		Path installTarget = Files.createTempDirectory("install-target");

		var repo = new RemoteModuleRepository<>(DummyExtensionInfo.class, List.of());

		// Führe Download und Verifikation durch
		repo.download(fileBaseUrl + "/test.zip", hash, installTarget);

		// Prüfung, ob Datei im entpackten Verzeichnis vorhanden ist
		boolean found = Files.walk(installTarget)
				.anyMatch(p -> p.getFileName().toString().equals("content.txt"));

		Assertions.assertThat(found)
				.as("content.txt sollte entpackt vorhanden sein")
				.isTrue();
	}
	
	@Test
	void shouldThrowAnExceptionOnInvalidSignature() throws Exception {

		// Zielverzeichnis
		Path installTarget = Files.createTempDirectory("install-target");

		var repo = new RemoteModuleRepository<>(DummyExtensionInfo.class, List.of());

		Assertions.assertThatCode(() -> {
			repo.download(fileBaseUrl + "/test.zip", "wrong_signature", installTarget);
		}).isInstanceOf(RuntimeException.class).hasMessage("error downloading module");
				
	}
}
