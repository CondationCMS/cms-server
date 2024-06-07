package com.github.thmarx.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class RemoteRepository {

	HttpClient client = HttpClient.newHttpClient();
	
	public boolean exists (String extension) {
		try {
			var moduleInfoUrl = "https://raw.githubusercontent.com/thmarx/extension-registry/main/%s/%s.yaml"
					.formatted(extension, extension);
			
			URI uri = URI.create(moduleInfoUrl);
			HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, BodyHandlers.ofString()).statusCode() == 200;
		} catch (IOException | InterruptedException ex) {
			log.error("", ex);
		}
		
		return false;
	}
	
	public Optional<String> getContent (String extension) {
		try {
			var moduleInfoUrl = "https://raw.githubusercontent.com/thmarx/extension-registry/main/%s/%s.js"
					.formatted(extension, extension);
			
			URI uri = URI.create(moduleInfoUrl);
			HttpRequest request = HttpRequest.newBuilder(uri).build();
			return Optional.of(client.send(request, BodyHandlers.ofString()).body());
		} catch (IOException | InterruptedException ex) {
			log.error("", ex);
		}
		
		return Optional.empty();
	}
	
	public Optional<ExtensionInfo> getInfo (String extension) {
		try {
			var moduleInfoUrl = "https://raw.githubusercontent.com/thmarx/extension-registry/main/%s/%s.yaml"
					.formatted(extension, extension);
			
			URI uri = URI.create(moduleInfoUrl);
			HttpRequest request = HttpRequest.newBuilder(uri).build();
			String content = client.send(request, BodyHandlers.ofString()).body();
			
			
			return Optional.of(new Yaml().loadAs(content, ExtensionInfo.class));
		} catch (IOException | InterruptedException ex) {
			log.error("", ex);
		}
		
		return Optional.empty();
	}
}
