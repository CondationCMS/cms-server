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

import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author thmar
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentPipelineFactory {
	
	public static ContentPipeline create (final RequestContext requestContext, final TemplateEngine.Model model) {
		
		var hookSystem = requestContext.get(HookSystemFeature.class).hookSystem();
		var pipeline = new ContentPipeline(new HookSystem(hookSystem), requestContext, model);
		pipeline.init();
		
		return pipeline;
	}
	
	
}
