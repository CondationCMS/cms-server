package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * example-module
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

import com.github.thmarx.cms.api.extensions.HttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.http.Request;
import com.github.thmarx.cms.api.extensions.http.Response;
import com.github.thmarx.modules.api.annotation.Extension;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author t.marx
 */
@Extension(HttpHandlerExtensionPoint.class)
public class ExampleHttpHandlerExtensionPoint extends HttpHandlerExtensionPoint {

    @Override
	public void execute(Request request, Response response) {
		response.write("hello from example module", StandardCharsets.UTF_8);
	}

	@Override
	public boolean handles(String method, String uri) {
		return "GET".equalsIgnoreCase(method) && uri.equalsIgnoreCase("test");
	}

	@Override
	public void init() {
		
	}
}
