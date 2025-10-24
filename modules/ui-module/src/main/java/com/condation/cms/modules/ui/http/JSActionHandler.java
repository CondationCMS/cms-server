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
import com.condation.cms.api.utils.HTTPUtil;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
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
public class JSActionHandler extends JettyHandler {

	private final FileSystem fileSystem;
	private final String base;
	private final SiteModuleContext context;
	
	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var resource = request.getHttpURI().getPath().replace(
				managerURL("/manager/actions/", context), "") + ".js";

		var files = fileSystem.getPath(base);

		if (resource.startsWith("/")) {
			resource = resource.substring(1);
		}

		var path = files.resolve(resource);
		if (Files.exists(path)) {
			response.getHeaders().put(HttpHeader.CONTENT_TYPE, "%s; charset=UTF-8".formatted(Files.probeContentType(path)));
			Content.Sink.write(response, true, Files.readString(path, StandardCharsets.UTF_8), callback);
		} else {
			callback.succeeded();
		}

		return true;
	}

}
