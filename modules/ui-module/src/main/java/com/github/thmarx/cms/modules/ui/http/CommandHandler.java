package com.github.thmarx.cms.modules.ui.http;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.modules.ui.services.CommandService;
import com.github.thmarx.cms.modules.ui.services.FileSystemService;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
@RequiredArgsConstructor
@Slf4j
public class CommandHandler extends JettyHandler {

	private final CommandService commandService;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		if (!request.getMethod().equalsIgnoreCase("POST")) {
			Response.writeError(request, response, callback, HttpStatus.METHOD_NOT_ALLOWED_405, "");
			return true;
		}

		String body = getBody(request);
		Optional<?> result = commandService.execute(body);
		
		
		response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json; charset=UTF-8");
		if (result.isPresent() && result.get() != null) {
			response.setStatus(200);
			Content.Sink.write(response, true, GSON.toJson(result.get()), callback);
		} else {
			response.setStatus(404);
			callback.succeeded();
		}
		

		return true;
	}

	
}
