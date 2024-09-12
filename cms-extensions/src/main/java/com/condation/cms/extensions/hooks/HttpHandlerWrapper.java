package com.condation.cms.extensions.hooks;

/*-
 * #%L
 * cms-extensions
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


import com.condation.cms.extensions.HttpHandlerExtension;
import com.condation.cms.extensions.http.ExtensionHttpHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

/**
 *
 * @author t.marx
 */
public class HttpHandlerWrapper {

	@Getter
	private final List<HttpHandlerExtension> httpHandlerExtensions = new ArrayList<>();

	public void add(final String method, final String path, final ExtensionHttpHandler handler) {
		httpHandlerExtensions.add(new HttpHandlerExtension(method, path, handler));
	}

	public Optional<HttpHandlerExtension> findHttpHandler(final String method, final String path) {
		return httpHandlerExtensions.stream().filter(handler -> handler.method().equalsIgnoreCase(method) && handler.path().equalsIgnoreCase(path)).findFirst();
	}
}
