package com.condation.cms.api.extensions;

/*-
 * #%L
 * CMS Api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author thmar
 */
public class Mapping {
	
	private List<PathHandler> handlers;
	
	private static record PathHandler (PathSpec pathSpec, HttpHandler httpHandler){}
	
	public Mapping () {
		handlers = new ArrayList<>();
	}
	
	public void add (@NonNull PathSpec pathSpec, @NonNull HttpHandler handler) {
		handlers.add(new PathHandler(pathSpec, handler));
	}
	
	public @NonNull Optional<HttpHandler> getMatchingHandler (@NonNull String uri) {
		return handlers.stream().filter(handler -> handler.pathSpec.matches(uri)).map(PathHandler::httpHandler).findFirst();
	}
	
	public @NonNull List<HttpHandler> getHandlers () {
		return handlers.stream().map(PathHandler::httpHandler).toList();
	}
}
