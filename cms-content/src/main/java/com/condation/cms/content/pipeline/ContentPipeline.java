package com.condation.cms.content.pipeline;

/*-
 * #%L
 * CMS Content
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
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.TemplateEngineFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.content.RenderContext;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class ContentPipeline {

	private final HookSystem hookSystem;
	private final RequestContext requestContext;
	private final TemplateEngine.Model model;
	
	protected void init() {

		List<String> pipeline = requestContext.get(ConfigurationFeature.class)
				.configuration().get(SiteConfiguration.class)
				.siteProperties().contentPipeline();

		AtomicInteger prio = new AtomicInteger(10);
		pipeline.forEach(processor -> {
			switch (processor) {
				case "markdown" -> hookSystem.<String>registerFilter(Hooks.CONTENT_FILTER.hook(), ctx -> processMarkdown(ctx.value()), prio.getAndAdd(10));
				case "tags" -> hookSystem.<String>registerFilter(Hooks.CONTENT_FILTER.hook(), ctx -> processTags(ctx.value()), prio.getAndAdd(10));
				case "template" -> hookSystem.<String>registerFilter(Hooks.CONTENT_FILTER.hook(), ctx -> processTemplate(ctx.value()), prio.getAndAdd(10));
			}
		});

	}
	
	public String process(String rawContent) {
		return hookSystem.doFilter(Hooks.CONTENT_FILTER.hook(), rawContent);
	}

	private String processMarkdown(String content) {
		return requestContext.get(RenderContext.class).markdownRenderer().render(content);
	}

	private String processTags(String content) {
		return requestContext.get(RenderContext.class).tags().replace(content, model.values, requestContext);
	}

	private String processTemplate(String content) {
		try {
			return requestContext.get(TemplateEngineFeature.class).templateEngine()
					.renderFromString(content, model);
		} catch (IOException ex) {
			log.error("", ex);
			return content;
		}
	}
}
