package com.github.thmarx.cms;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.request.features.IsDevModeFeature;
import com.github.thmarx.cms.api.request.features.IsPreviewFeature;
import com.github.thmarx.cms.api.request.features.RequestFeature;
import com.github.thmarx.cms.content.ContentTags;
import com.github.thmarx.cms.request.RenderContext;
import com.github.thmarx.cms.request.RequestExtensions;
import com.github.thmarx.cms.theme.DefaultTheme;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public abstract class TestHelper {

	public static MarkdownRenderer getRenderer() {
		return new TestMarkdownRenderer();
	}
	
	public static RequestContext requestContext() {
		return requestContext("");
	}
	
	public static RequestContext requestContext(String uri) {
		var markdownRenderer = TestHelper.getRenderer();
		RequestContext context = new RequestContext();
		context.add(RequestFeature.class, new RequestFeature(uri, Map.of()));
		context.add(RequestExtensions.class, new RequestExtensions(null, null));
		context.add(RenderContext.class, new RenderContext(markdownRenderer, new ContentTags(Map.of()), DefaultTheme.EMPTY));
		
		context.add(IsPreviewFeature.class, new IsPreviewFeature(false));
		context.add(IsDevModeFeature.class, new IsDevModeFeature(false));
		
		return context;
	}

}
