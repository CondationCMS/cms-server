package com.github.thmarx.cms.request;

/*-
 * #%L
 * cms-server
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
