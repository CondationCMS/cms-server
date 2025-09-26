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
import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.modules.ui.model.HookCall;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import java.util.HashMap;
import java.util.Map;
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
public class HookHandler extends JettyHandler {

	private final HookSystem hookSystem;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		if (!request.getMethod().equalsIgnoreCase("POST")) {
			Response.writeError(request, response, callback, HttpStatus.METHOD_NOT_ALLOWED_405, "");
			return true;
		}

		String body = getBody(request);
		var command = UIGsonProvider.INSTANCE.fromJson(body, HookCall.class);
		ActionContext<?> actionContext  = hookSystem.execute(command.hook(), command.parameters());
		
		Map<String, Object> commandResponse = new HashMap<>();
		commandResponse.put("hook", command.hook());
		if (!actionContext.results().isEmpty()) {
			commandResponse.put("result", actionContext.results());
		}
		
		response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json; charset=UTF-8");
		response.setStatus(200);
		Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(commandResponse), callback);
		

		return true;
	}

	
}
