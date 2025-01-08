package com.condation.cms.api.model;

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


import com.condation.cms.api.request.RequestContext;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class Parameter extends HashMap<String, Object> {
	
	private RequestContext requestContext = null;
	
	public Parameter () {
	}
	
	public Parameter (final RequestContext requestContext) {
		this.requestContext = requestContext;
	}
	
	public Parameter (final Map<String, Object> parameters) {
		super(parameters);
	}
	
	public Parameter (final Map<String, Object> parameters, final RequestContext requestContext) {
		super(parameters);
		this.requestContext = requestContext;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}
}
