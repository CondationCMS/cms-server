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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

@Slf4j
@RequiredArgsConstructor
public class RemoteRepository {

	private final HttpClient client = HttpClient.newHttpClient();
	private final List<String> baseUrls;

	public boolean exists(String extension) {
		return baseUrls.stream().anyMatch(baseUrl -> {
			String url = baseUrl + "%s%s/%s.yaml".formatted(
					baseUrl.endsWith("/") ? "" : "/",
					extension, 
					extension);
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
				int statusCode = client.send(request, BodyHandlers.ofString()).statusCode();
				return statusCode == 200;
			} catch (IOException | InterruptedException ex) {
				log.warn("Failed checking existence at {}: {}", url, ex.getMessage());
				return false;
			}
		});
	}

	public Optional<String> getContent(String extension) {
		for (String baseUrl : baseUrls) {
			String url = baseUrl + "%s%s/%s.js".formatted(
					baseUrl.endsWith("/") ? "" : "/",
					extension, 
					extension);
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
				String body = client.send(request, BodyHandlers.ofString()).body();
				return Optional.ofNullable(body);
			} catch (IOException | InterruptedException ex) {
				log.warn("Failed loading content from {}: {}", url, ex.getMessage());
			}
		}
		return Optional.empty();
	}

	public Optional<ExtensionInfo> getInfo(String extension) {
		for (String baseUrl : baseUrls) {
			String url = baseUrl + "%s%s/%s.yaml".formatted(
					baseUrl.endsWith("/") ? "" : "/",
					extension, 
					extension);
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
				String content = client.send(request, BodyHandlers.ofString()).body();
				ExtensionInfo info = new Yaml().loadAs(content, ExtensionInfo.class);
				return Optional.of(info);
			} catch (IOException | InterruptedException ex) {
				log.warn("Failed loading info from {}: {}", url, ex.getMessage());
			}
		}
		return Optional.empty();
	}
}
