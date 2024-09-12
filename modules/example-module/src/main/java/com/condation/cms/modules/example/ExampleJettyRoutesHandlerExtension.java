package com.condation.cms.modules.example;

/*-
 * #%L
 * example-module
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import com.condation.cms.api.extensions.HttpRoutesExtensionPoint;
import com.condation.cms.api.extensions.Mapping;
import com.condation.modules.api.annotation.Extension;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Extension(HttpRoutesExtensionPoint.class)
public class ExampleJettyRoutesHandlerExtension extends HttpRoutesExtensionPoint {

	@Override
	public Mapping getMapping() {
		Mapping mapping = new Mapping();
		mapping.add(PathSpec.from("/world1"), new ExampleHandler("Hello world1!"));
		mapping.add(PathSpec.from("/people1"), new ExampleHandler("Hello people1!"));
		
		return mapping;
	}
	
	@RequiredArgsConstructor
	public static class ExampleHandler implements HttpHandler {

		private final String message;
		
		@Override
		public boolean handle(Request request, Response response, Callback callback) throws Exception {
			response.write(true, ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), callback);
			return true;
		}
		
	}
}
