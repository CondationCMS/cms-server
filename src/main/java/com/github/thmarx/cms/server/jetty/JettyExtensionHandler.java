package com.github.thmarx.cms.server.jetty;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.extensions.HttpHandlerExtension;
import com.github.thmarx.cms.extensions.http.jetty.JettyHttpHandlerWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyExtensionHandler extends Handler.Abstract {

	private final ExtensionManager extensionManager;


	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		try (var context = extensionManager.newContext()) {
			String extension = getExtensionName(request);
			var method = request.getMethod();
			Optional<HttpHandlerExtension> findHttpHandler = context.findHttpHandler(method, extension);
			if (findHttpHandler.isEmpty()) {
				response.setStatus(404);
				callback.succeeded();
				return false;
			}
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
		}
		
	}

	private String getExtensionName(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();
		path = path.replace(contextPath, "");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

}
