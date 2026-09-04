package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * UI Module
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
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.extensions.UIScriptActionSourceExtension;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class JSActionHandler extends JettyHandler {
	private static final String JAVASCRIPT_EXTENSION = ".js";

	private final FileSystem fileSystem;
	private final String base;
	private final SiteModuleContext context;
	
	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var resourceName = request.getHttpURI().getPath().replace(
				managerURL("/manager/actions/", context), "");
		
		if (resourceName.startsWith("/")) {
			resourceName = resourceName.substring(1);
		}
		
		String scriptContent = "";
		
		var moduleContent = getScriptContentFromModules(resourceName);
		if (moduleContent.isPresent()) {
			scriptContent = moduleContent.get();
		} else {
			var bundledScript = getBundledScript(resourceName);
			scriptContent = bundledScript.isPresent()
					? bundledScript.get()
					: getScriptFromFileSystem(resourceName).orElse("");
		}
		
		
		if (!Strings.isNullOrEmpty(scriptContent)) {
			response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/javascript; charset=UTF-8");
				Content.Sink.write(response, true, scriptContent, callback);
		} else {
			response.setStatus(HttpStatus.NOT_FOUND_404);
			callback.succeeded();
		}

		return true;
	}

	Optional<String> getBundledScript(String resourceName) {
		var resourcePath = "%s/%s".formatted(base, scriptResourceName(resourceName));
		try (var stream = JSActionHandler.class.getResourceAsStream(resourcePath)) {
			if (stream == null) {
				return Optional.empty();
			}
			return Optional.of(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException exception) {
			log.error("Could not load manager action {}", resourcePath, exception);
			return Optional.empty();
		}
	}

	private Optional<String> getScriptFromFileSystem(String resourceName) {
		var resourceFile = scriptResourceName(resourceName);
		var files = fileSystem.getPath(base);
		var path = files.resolve(resourceFile);
		if (!Files.exists(path)) {
			return Optional.empty();
		}
		try {
			return Optional.of(Files.readString(path));
		} catch (IOException exception) {
			log.error("Could not load manager action {}", path, exception);
			return Optional.empty();
		}
	}

	private String scriptResourceName(String resourceName) {
		return resourceName.endsWith(JAVASCRIPT_EXTENSION)
				? resourceName
				: resourceName + JAVASCRIPT_EXTENSION;
	}
	
	private Optional<String> getScriptContentFromModules (String filename) {
		return context.get(ModuleManagerFeature.class).moduleManager().extensions(UIScriptActionSourceExtension.class)
				.stream()
				.map(UIScriptActionSourceExtension::getActionSources)
				.filter(source -> source.containsKey(filename))
				.map(source -> source.get(filename))
				.findFirst();
	}

}
