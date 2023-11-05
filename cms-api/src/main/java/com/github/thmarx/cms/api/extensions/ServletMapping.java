package com.github.thmarx.cms.api.extensions;

/*-
 * #%L
 * cms-api
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

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.eclipse.jetty.http.pathmap.PathSpec;

/**
 *
 * @author thmar
 */
public class ServletMapping {
	
	@Getter
	private Map<String, HttpServlet> servletMappings;
	
	public ServletMapping () {
		servletMappings = new HashMap<>();
	}
	
	public void add (String pathSpec, HttpServlet servlet) {
		servletMappings.put(pathSpec, servlet);
	}
	
	public Optional<HttpServlet> getMatchingServlet (String uri) {
		return servletMappings.entrySet().stream().filter(entry -> entry.getKey().matches(uri)).map(entry -> entry.getValue()).findFirst();
	}
	
	public List<HttpServlet> getServlets () {
		return new ArrayList<>(servletMappings.values());
	}
}
