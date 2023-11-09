package com.github.thmarx.cms.module;

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

import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.RequestContext;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.content.ContentTags;
import com.github.thmarx.cms.extensions.ExtensionManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class RenderContentFunction implements BiFunction<String, Map<String, List<String>>, Optional<String>> {

	private final Supplier<ContentResolver> contentResolver;
	private final Supplier<ExtensionManager> manager;

	private final Function<Context, MarkdownRenderer> markdownRendererProvider;
	
	@Override
	public Optional<String> apply(String uri, Map<String, List<String>> parameters) {
		try (
				var contextHolder = manager.get().newContext(); 
				final MarkdownRenderer markdownRenderer = markdownRendererProvider.apply(contextHolder.getContext());) {

			RequestContext context = new RequestContext(uri, parameters,
					new RenderContext(contextHolder, markdownRenderer, new ContentTags(contextHolder.getTags())));
			return contentResolver.get().getContent(context);
		} catch (Exception e) {
			log.error("", e);
		}
		return Optional.empty();
	}
	
}
