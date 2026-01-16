package com.condation.cms.templates.filter.impl;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.feature.features.MarkdownRendererFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.templates.filter.Filter;

/**
 *
 * @author thmar
 */
public class MarkdownFilter implements Filter {
	
	public static final String NAME = "markdown";

	@Override
	public Object apply(Object input, Object... params) {
		if (input == null || !(input instanceof String)) {
			return input;
		}

		var requestContext = RequestContextScope.REQUEST_CONTEXT.get();

		if (requestContext.has(MarkdownRendererFeature.class)) {
			return requestContext.get(MarkdownRendererFeature.class).markdownRenderer().render((String)input);
		}

		return input;
	}
}
