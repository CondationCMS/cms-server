package com.condation.cms.api.hooks;

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


/**
 *
 * @author t.marx
 */
public enum Hooks {
	
	NAVIGATION_PATH("system/navigation/%s/path"),
	NAVIGATION_LIST("system/navigation/%s/list"),
	CONTENT_SHORTCODE("system/content/shortcodes"),
	CONTENT_FILTER("system/content/filter"),
	DB_QUERY_OPERATIONS("system/db/query/operations"),
	SCHEDULER_REGISTER("system/scheduler/register"),
	SCHEDULER_REMOVE("system/scheduler/remove"),
	HTTP_EXTENSION("system/server/http/extension"),
	HTTP_ROUTE("system/server/http/route"),
	API_ROUTE("system/server/api/route"),
	TEMPLATE_SUPPLIER("system/template/supplier"),
	TEMPLATE_FUNCTION("system/template/function")
	;
	
	private String pattern;
	
	private Hooks (String pattern) {
		this.pattern = pattern;
	}
	
	public String hook () {
		return pattern;
	}
	
	public String hook (Object...variables) {
		return pattern.formatted(variables);
	}
}
