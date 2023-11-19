package com.github.thmarx.cms.api.extensions;

/*-
 * #%L
 * cms-api
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;

/**
 *
 * @author thmar
 */
public class Mapping {
	
	private Map<PathSpec, Handler> handlerMapping;
	
	public Mapping () {
		handlerMapping = new HashMap<>();
	}
	
	public void add (PathSpec pathSpec, Handler handler) {
		handlerMapping.put(pathSpec, handler);
	}
	
	public Optional<Handler> getMatchingHandler (String uri) {
		return handlerMapping.entrySet().stream().filter(entry -> entry.getKey().matches(uri)).map(entry -> entry.getValue()).findFirst();
	}
	
	public List<Handler> getHandlers () {
		return new ArrayList<>(handlerMapping.values());
	}
}
