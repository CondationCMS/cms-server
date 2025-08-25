package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * ui-module
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
import com.condation.cms.api.module.SiteModuleContext;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
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
public class PublicResourceHandler extends JettyHandler {

	private final SiteModuleContext context;
	private final FileSystem fileSystem;
	private final String base;
	private final List<String> publicResources;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		var resource = request.getHttpURI().getPath().replace(
				managerURL("/manager/", context), "");

		if (resource.equals("")) {
			response.setStatus(404);
			callback.succeeded();
			return true;
		}
		if (!publicResources.contains(resource)) {
			response.setStatus(403);
			callback.succeeded();
			return true;
		}

		var files = fileSystem.getPath(base);

		if (resource.startsWith("/")) {
			resource = resource.substring(1);
		}

		var path = files.resolve(resource);
		if (Files.exists(path)) {
			String contentType = Files.probeContentType(path);
			response.getHeaders().put(HttpHeader.CONTENT_TYPE, "%s; charset=UTF-8".formatted(contentType));

			String fileName = path.getFileName().toString();
			boolean useString = fileName.endsWith(".js") || fileName.endsWith(".css") || fileName.endsWith(".map");

			if (useString) {
				String content = Files.readString(path, StandardCharsets.UTF_8);
				Content.Sink.write(response, true, content, callback);
			} else {
				byte[] bytes = Files.readAllBytes(path);
				Content.Sink.write(response, true, ByteBuffer.wrap(bytes));
				callback.succeeded();
			}
		} else {
			callback.succeeded();
		}

		return true;
	}

}
