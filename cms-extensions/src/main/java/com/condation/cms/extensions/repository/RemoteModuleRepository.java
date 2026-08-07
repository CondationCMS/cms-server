package com.condation.cms.extensions.repository;

/*-
 * #%L
 * CMS Extensions
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
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.api.utils.SecureFileUtils;
import com.condation.cms.core.utils.HashVerifier;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	public void download(String url, String signature, Path target) {
		Path tempDirectory = null;
		try {
			Path normalizedTarget = target.toAbsolutePath().normalize();
			Files.createDirectories(normalizedTarget);
			normalizedTarget = normalizedTarget.toRealPath();
			Path targetParent = normalizedTarget.getParent();
			if (targetParent == null) {
				throw new IOException("Module target must have a parent: " + target);
			}

			Path workRoot = SecureFileUtils.ensurePrivateDirectory(
					targetParent.resolve("." + normalizedTarget.getFileName() + "-module-work"));
			tempDirectory = SecureFileUtils.createPrivateTempDirectory(workRoot, "module-");

			HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
			HttpResponse<Path> response = client.send(
					request,
					HttpResponse.BodyHandlers.ofFile(tempDirectory.resolve("download.zip")));
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IOException("Module download failed with HTTP status " + response.statusCode());
			}

			Path downloaded = response.body();

			if (!HashVerifier.verifySHA256(downloaded, signature)) {
				throw new IOException("Module signature does not match");
			}

			File moduleTempDir = InstallationHelper.unpackArchive(downloaded.toFile(), tempDirectory.toFile());
			if (moduleTempDir == null || moduleTempDir.getName().isBlank()) {
				throw new IOException("Downloaded archive does not contain a module directory");
			}
			Path destination = normalizedTarget.resolve(moduleTempDir.getName()).normalize();
			if (!destination.startsWith(normalizedTarget)) {
				throw new IOException("Invalid module directory in downloaded archive");
			}
			if (!InstallationHelper.moveDirectoy(moduleTempDir, destination.toFile())) {
				throw new IOException("Could not install module into " + destination);
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			log.error("Module download was interrupted", ex);
			throw new RuntimeException("error downloading module", ex);
		} catch (Exception ex) {
			log.error("Error downloading module: {}", ex.getMessage(), ex);
			throw new RuntimeException("error downloading module", ex);
		} finally {
			if (tempDirectory != null) {
				try {
					FileUtils.deleteFolder(tempDirectory);
				} catch (IOException cleanupException) {
					log.warn("Could not remove module download work directory {}", tempDirectory,
							cleanupException);
				}
			}
		}
	}
}
