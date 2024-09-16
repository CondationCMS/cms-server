package com.condation.cms.content.pipeline;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.RenderContext;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentPipeline {

	private final HookSystem hookSystem;
	private final RequestContext requestContext;
	
	void init () {
		hookSystem.registerFilter(Hooks.CONTENT_FILTER.hook(), this::markdown, 20);
		hookSystem.registerFilter(Hooks.CONTENT_FILTER.hook(), this::shortCodes, 30);
	}
	
	public String process (String rawContent) {
		return hookSystem.filter(Hooks.CONTENT_FILTER.hook(), rawContent).value();
	}
	
	private String markdown (FilterContext<String> context) {
		return requestContext.get(RenderContext.class).markdownRenderer().render(context.value());
	}
	
	private String shortCodes (FilterContext<String> context) {
		return requestContext.get(RenderContext.class).shortCodes().replace(context.value());
	}
	
	private <F extends Feature> Object getFeatureValueOrDefault(RequestContext context,
			Class<F> feature, Function<F, Object> valueFunction, Object defaultValue) {
		if (context.has(feature)) {
			return valueFunction.apply(context.get(feature));
		}
		return defaultValue;
	}
}
