package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.templates.functions.TemplateFunction;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class UriParamFunction implements TemplateFunction {

	public static final String FUNCTION_NAME = "uri_param";

	@Override
	public Object invoke(Object... params) {
		if (params == null || params.length == 0 || !(params[0] instanceof String)) {
			return "";
		}
		
		return URLEncoder.encode(String.valueOf(params[0]), StandardCharsets.UTF_8)
                .replace("+", "%20")      // Leerzeichen als %20, nicht +
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
	}

	@Override
	public String name() {
		return FUNCTION_NAME;
	}
	
}
