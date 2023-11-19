package com.github.thmarx.cms.request;

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
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.content.ContentTags;
import com.github.thmarx.cms.extensions.ExtensionManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class RequestContextFactory {
	private final Supplier<MarkdownRenderer> markdownRenderer;
	private final ExtensionManager extensionManager;
	private final Theme theme;

	
	
	public RequestContext create (String uri, Map<String, List<String>> queryParameters) throws IOException {
		
		var requestTheme = new RequestTheme(theme);
		
		RequestExtensions requestExtensions = extensionManager.newContext(requestTheme);
		
		RenderContext renderContext = new RenderContext(
				markdownRenderer.get(), 
				new ContentTags(requestExtensions.getTags()), 
				requestTheme);
		
		RequestContext requestContext = new RequestContext(uri, queryParameters, requestExtensions, renderContext);
		
		return requestContext;
	}
}
