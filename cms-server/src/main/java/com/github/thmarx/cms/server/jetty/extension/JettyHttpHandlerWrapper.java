package com.github.thmarx.cms.server.jetty.extension;

/*-
 * #%L
 * cms-server
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


import com.github.thmarx.cms.api.extensions.http.ExtensionHttpHandler;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class JettyHttpHandlerWrapper extends Handler.Abstract {
	
	private final ExtensionHttpHandler handler;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		handler.execute(new JettyRequest(request), new JettyResponse(response, callback));
		return true;
	}
	
	
	
}
