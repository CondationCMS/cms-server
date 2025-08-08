package com.condation.cms;

/*-
 * #%L
 * cms-server
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


import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.ContentNodeMapperFeature;
import com.condation.cms.api.feature.features.ContentParserFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.MarkdownRendererFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.feature.features.ServerPropertiesFeature;
import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.RenderContext;
import com.condation.cms.content.tags.Tags;
import com.condation.cms.content.tags.TagParser;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedServerProperties;
import com.condation.cms.extensions.hooks.DBHooks;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.extensions.request.RequestExtensions;
import com.condation.cms.media.FileMediaService;
import com.condation.cms.core.theme.DefaultTheme;
import com.condation.cms.test.TestSiteProperties;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.mockito.Mockito;

/**
 *
 * @author t.marx
 */
public abstract class TestHelper {

	public static MarkdownRenderer getRenderer() {
		return new TestMarkdownRenderer();
	}

	public static RequestContext requestContext() throws IOException {
		return requestContext("");
	}

	public static RequestContext requestContext(String uri) throws IOException {
		var markdownRenderer = TestHelper.getRenderer();
		RequestContext context = new RequestContext();
		
		var tagparser = new TagParser(new JexlBuilder().create());
		
		context.add(RequestFeature.class, new RequestFeature(uri, Map.of()));
		context.add(RequestExtensions.class, new RequestExtensions(null, null));
		context.add(RenderContext.class, new RenderContext(markdownRenderer, new Tags(Map.of(), tagparser), DefaultTheme.NO_THEME));

		context.add(SiteMediaServiceFeature.class, new SiteMediaServiceFeature(new FileMediaService(null)));
		context.add(InjectorFeature.class, new InjectorFeature(Mockito.mock(Injector.class)));
		context.add(HookSystemFeature.class, new HookSystemFeature(new HookSystem()));
		
		context.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(null));
		context.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(null));
		context.add(ContentParserFeature.class, new ContentParserFeature(null));
		
		final SiteProperties siteProperties = new TestSiteProperties(Map.of(
				"context_path", "/"
		));
		context.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
		
		Configuration config = Mockito.mock(Configuration.class);
		SiteConfiguration siteConfig = Mockito.mock(SiteConfiguration.class);
		Mockito.when(siteConfig.siteProperties()).thenReturn(siteProperties);
		Mockito.when(config.get(SiteConfiguration.class)).thenReturn(siteConfig);
		context.add(ConfigurationFeature.class, new ConfigurationFeature(config));
		
		context.add(
				ServerPropertiesFeature.class, 
				new ServerPropertiesFeature(new ExtendedServerProperties(ConfigurationFactory.serverConfiguration()))
		);
		
		context.add(TemplateHooks.class, new TemplateHooks(context));
		context.add(DBHooks.class, new DBHooks(context));

		return context;
	}

	public static RequestContext requestContext(String uri, ContentParser contentParser, MarkdownRenderer markdownRenderer, ContentNodeMapper contentMapper) throws IOException {

		RequestContext context = requestContext(uri);
		context.add(ContentParserFeature.class, new ContentParserFeature(contentParser));
		context.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(markdownRenderer));
		context.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(contentMapper));
		context.add(HookSystemFeature.class, new HookSystemFeature(new HookSystem()));
		context.add(SitePropertiesFeature.class, new SitePropertiesFeature(new TestSiteProperties(Map.of(
				"context_path", "/"
		))));

		return context;
	}
}
