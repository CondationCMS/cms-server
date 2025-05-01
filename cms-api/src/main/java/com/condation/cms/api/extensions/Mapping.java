package com.condation.cms.api.extensions;

/*-
 * #%L
 * cms-api
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.http.pathmap.PathSpec;

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
	
	public void add (PathSpec pathSpec, HttpHandler handler) {
		handlers.add(new PathHandler(pathSpec, handler));
	}
	
	public Optional<HttpHandler> getMatchingHandler (String uri) {
		return handlers.stream().filter(handler -> handler.pathSpec.matches(uri)).map(PathHandler::httpHandler).findFirst();
	}
	
	public List<HttpHandler> getHandlers () {
		return handlers.stream().map(PathHandler::httpHandler).toList();
	}
}
