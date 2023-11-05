package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * example-module
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

import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.Mapping;
import com.github.thmarx.cms.api.extensions.ServletExtensionPoint;
import com.github.thmarx.cms.api.extensions.ServletMapping;
import com.github.thmarx.modules.api.annotation.Extension;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Extension(ServletExtensionPoint.class)
public class ExampleServletExtension extends ServletExtensionPoint {

	

	@Override
	public ServletMapping getMapping() {
		ServletMapping mapping = new ServletMapping();
		mapping.add("/world", new ExampleHandler("Hello world!"));
		mapping.add("/people", new ExampleHandler("Hello people!"));
		
		return mapping;
	}
	
	@RequiredArgsConstructor
	public static class ExampleHandler extends HttpServlet {

		private final String message;
		
		@Override
		public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(message);
		}
		
	}
}
