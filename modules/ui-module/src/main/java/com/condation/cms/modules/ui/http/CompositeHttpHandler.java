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

import com.condation.cms.api.extensions.HttpHandler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import java.util.List;

public class CompositeHttpHandler implements HttpHandler {

	private final List<HttpHandler> handlers;

	public CompositeHttpHandler(List<HttpHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		for (HttpHandler handler : handlers) {
			if (handler.handle(request, response, callback)) {
				return true; // Stop, this handler has handled the request
			}
		}
		return false; // No handler handled the request
	}
}
