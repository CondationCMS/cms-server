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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 * @param <T>
 */
@Slf4j
@RequiredArgsConstructor
public class RemoteModuleRepository<T> {

	static HttpClient client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.followRedirects(HttpClient.Redirect.ALWAYS)
			.build();

	private final Class<T> type;
	private final String baseUrl;

	public boolean exists(String id) {
		try {
			var moduleInfoUrl = baseUrl + "/main/%s/%s.yaml"
					.formatted(id, id);

			URI uri = URI.create(moduleInfoUrl);
			HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
		} catch (IOException | InterruptedException ex) {
			log.error("", ex);
		}

		return false;
	}

	public Optional<T> getInfo(String extension) {
		try {
			var moduleInfoUrl = baseUrl + "/main/%s/%s.yaml"
					.formatted(extension, extension);

			URI uri = URI.create(moduleInfoUrl);
			HttpRequest request = HttpRequest.newBuilder(uri)
					.build();
			final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			String content = response.body();

			return Optional.of(new Yaml().loadAs(content, type));
		} catch (IOException | InterruptedException ex) {
			log.error("", ex);
		}

		return Optional.empty();
	}

	public void download(String url, Path target) {

		try {
			Path tempDirectory = Files.createTempDirectory("modules");
			if (SystemUtils.IS_OS_UNIX) {
				Files.setPosixFilePermissions(tempDirectory, PosixFilePermissions.fromString("rwx------"));
			} else {
				var f = tempDirectory.toFile();
				f.setReadable(true, true);
				f.setWritable(true, true);
				f.setExecutable(true, true);
			}

			var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
			HttpResponse<Path> response = client.send(
					request,
					HttpResponse.BodyHandlers.ofFile(tempDirectory.resolve(System.currentTimeMillis() + ".zip")));

			var downloaded = response.body();

			File moduleTempDir = InstallationHelper.unpackArchive(downloaded.toFile(), tempDirectory.toFile());

			InstallationHelper.moveDirectoy(moduleTempDir, target.resolve(moduleTempDir.getName()).toFile());
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException("error downloading module");
		}
	}
}
