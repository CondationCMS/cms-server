package com.github.thmarx.cms.server.jetty.extension;

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
