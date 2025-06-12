package com.condation.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.yaml.snakeyaml.Yaml;

@Slf4j
@RequiredArgsConstructor
public class RemoteModuleRepository<T> {

	static HttpClient client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.followRedirects(HttpClient.Redirect.ALWAYS)
			.build();

	private final Class<T> type;
	private final List<String> baseUrls;

	public boolean exists(String id) {
		for (String baseUrl : baseUrls) {
			try {
				String moduleInfoUrl = baseUrl + "/%s/%s.yaml".formatted(id, id);
				URI uri = URI.create(moduleInfoUrl);
				HttpRequest request = HttpRequest.newBuilder(uri).build();
				int status = client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
				if (status == 200) {
					return true;
				}
			} catch (IOException | InterruptedException ex) {
				log.warn("Failed checking existence at {}: {}", baseUrl, ex.getMessage());
			}
		}
		return false;
	}

	public Optional<T> getInfo(String extension) {
		for (String baseUrl : baseUrls) {
			try {
				String moduleInfoUrl = baseUrl + "/%s/%s.yaml".formatted(extension, extension);
				URI uri = URI.create(moduleInfoUrl);
				HttpRequest request = HttpRequest.newBuilder(uri).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					String content = response.body();
					return Optional.of(new Yaml().loadAs(content, type));
				}
			} catch (IOException | InterruptedException ex) {
				log.warn("Failed loading info from {}: {}", baseUrl, ex.getMessage());
			}
		}
		return Optional.empty();
	}

	public void download(String url, Path target) {
		try {
			Path tempDirectory = Files.createTempDirectory("modules");
			if (SystemUtils.IS_OS_UNIX) {
				Files.setPosixFilePermissions(tempDirectory, PosixFilePermissions.fromString("rwx------"));
			} else {
				File f = tempDirectory.toFile();
				f.setReadable(true, true);
				f.setWritable(true, true);
				f.setExecutable(true, true);
			}

			HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
			HttpResponse<Path> response = client.send(
					request,
					HttpResponse.BodyHandlers.ofFile(tempDirectory.resolve(System.currentTimeMillis() + ".zip")));

			Path downloaded = response.body();
			File moduleTempDir = InstallationHelper.unpackArchive(downloaded.toFile(), tempDirectory.toFile());
			InstallationHelper.moveDirectoy(moduleTempDir, target.resolve(moduleTempDir.getName()).toFile());

		} catch (Exception ex) {
			log.error("Error downloading module: {}", ex.getMessage(), ex);
			throw new RuntimeException("error downloading module");
		}
	}
}
