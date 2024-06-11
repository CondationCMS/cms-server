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
import com.github.thmarx.cms.extensions.request.RequestExtensions;
import com.github.thmarx.cms.content.RenderContext;
import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.configuration.configs.ServerConfiguration;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.extensions.RegisterShortCodesExtensionPoint;
import com.github.thmarx.cms.api.feature.features.ConfigurationFeature;
import com.github.thmarx.cms.api.feature.features.ContentNodeMapperFeature;
import com.github.thmarx.cms.api.feature.features.ContentParserFeature;
import com.github.thmarx.cms.api.hooks.HookSystem;
import com.github.thmarx.cms.api.mapper.ContentNodeMapper;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.media.MediaService;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.feature.features.HookSystemFeature;
import com.github.thmarx.cms.api.feature.features.InjectorFeature;
import com.github.thmarx.cms.api.feature.features.IsDevModeFeature;
import com.github.thmarx.cms.api.feature.features.IsPreviewFeature;
import com.github.thmarx.cms.api.feature.features.MarkdownRendererFeature;
import com.github.thmarx.cms.api.feature.features.RequestFeature;
import com.github.thmarx.cms.api.feature.features.ServerPropertiesFeature;
import com.github.thmarx.cms.api.feature.features.SiteMediaServiceFeature;
import com.github.thmarx.cms.api.feature.features.SitePropertiesFeature;
import com.github.thmarx.cms.api.feature.features.ThemeFeature;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.content.shortcodes.ShortCodes;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.utils.HTTPUtil;
import com.github.thmarx.cms.api.utils.RequestUtil;
import com.github.thmarx.modules.api.ModuleManager;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class RequestContextFactory {

	private final Injector injector;

	public RequestContext create(
			Request request) throws IOException {
		
//		var uri = request.getHttpURI().getPath();
		var uri = RequestUtil.getContentPath(request);
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		
		return create(uri, queryParameters);
	}
	
	public RequestContext create(
			String uri, Map<String, List<String>> queryParameters) throws IOException {

		var hookSystem = injector.getInstance(HookSystem.class);
		var theme = injector.getInstance(Theme.class);
		var markdownRenderer = injector.getInstance(MarkdownRenderer.class);
		var extensionManager = injector.getInstance(ExtensionManager.class);
		var siteProperties = injector.getInstance(SiteProperties.class);
		var siteMediaService = injector.getInstance(MediaService.class);
		
		var requestContext = new RequestContext();
		requestContext.add(InjectorFeature.class, new InjectorFeature(injector));
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));
		requestContext.add(RequestFeature.class, new RequestFeature(uri, queryParameters));
		requestContext.add(ThemeFeature.class, new ThemeFeature(theme));
		requestContext.add(ContentParserFeature.class, new ContentParserFeature(injector.getInstance(ContentParser.class)));
		requestContext.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(injector.getInstance(ContentNodeMapper.class)));
		if (ServerContext.IS_DEV) {
			requestContext.add(IsDevModeFeature.class, new IsDevModeFeature());

			if (queryParameters.containsKey("preview")) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature());
			}
		}
		requestContext.add(ConfigurationFeature.class, new ConfigurationFeature(injector.getInstance(Configuration.class)));
		requestContext.add(ServerPropertiesFeature.class, new ServerPropertiesFeature(
				injector.getInstance(Configuration.class)
				.get(ServerConfiguration.class).serverProperties()
		));
		requestContext.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
		requestContext.add(SiteMediaServiceFeature.class, new SiteMediaServiceFeature(siteMediaService));
		
		RequestExtensions requestExtensions = extensionManager.newContext(theme, requestContext);

		RenderContext renderContext = new RenderContext(
				markdownRenderer,
				createShortCodes(requestExtensions),
				theme);
		requestContext.add(RenderContext.class, renderContext);
		requestContext.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(renderContext.markdownRenderer()));

		
		requestContext.add(RequestExtensions.class, requestExtensions);
		

		return requestContext;
	}
	
	private ShortCodes createShortCodes (RequestExtensions requestExtensions) {
		var codes = requestExtensions.getShortCodes();
		
		injector.getInstance(ModuleManager.class).extensions(RegisterShortCodesExtensionPoint.class)
				.forEach(extension -> codes.putAll(extension.shortCodes()));
		
		return new ShortCodes(codes);
	}

}
