package com.condation.cms.request;

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
import com.condation.cms.api.ServerContext;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.extensions.RegisterShortCodesExtensionPoint;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.ContentNodeMapperFeature;
import com.condation.cms.api.feature.features.ContentParserFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.MarkdownRendererFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.feature.features.ServerPropertiesFeature;
import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.feature.features.TemplateEngineFeature;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.media.MediaService;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.content.RenderContext;
import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.content.shortcodes.TagParser;
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.extensions.hooks.ContentHooks;
import com.condation.cms.extensions.hooks.DBHooks;
import com.condation.cms.extensions.hooks.ServerHooks;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.extensions.request.RequestExtensions;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class RequestContextFactory {

	private final Injector injector;

	public RequestContext createContext () {
		var requestContext = new RequestContext();
		
		var theme = injector.getInstance(Theme.class);
//		var siteProperties = injector.getInstance(SiteProperties.class);
		var siteMediaService = injector.getInstance(MediaService.class);

		requestContext.add(InjectorFeature.class, new InjectorFeature(injector));

		requestContext.add(ThemeFeature.class, new ThemeFeature(theme));
		requestContext.add(ContentParserFeature.class, new ContentParserFeature(injector.getInstance(ContentParser.class)));
		requestContext.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(injector.getInstance(ContentNodeMapper.class)));
		if (ServerContext.IS_DEV) {
			requestContext.add(IsDevModeFeature.class, new IsDevModeFeature());
		}
		final Configuration configuration = injector.getInstance(Configuration.class);
		requestContext.add(ConfigurationFeature.class, new ConfigurationFeature(configuration));
		requestContext.add(ServerPropertiesFeature.class, 
				new ServerPropertiesFeature(
				configuration.get(ServerConfiguration.class).serverProperties()));
		requestContext.add(SitePropertiesFeature.class, 
				new SitePropertiesFeature(
						configuration.get(SiteConfiguration.class).siteProperties()));
		
		requestContext.add(SiteMediaServiceFeature.class, new SiteMediaServiceFeature(siteMediaService));

		requestContext.add(ServerHooks.class, new ServerHooks(requestContext));
		requestContext.add(TemplateHooks.class, new TemplateHooks(requestContext));
		requestContext.add(DBHooks.class, new DBHooks(requestContext));
		requestContext.add(ContentHooks.class, new ContentHooks(requestContext));

		requestContext.add(HookSystemFeature.class, new HookSystemFeature(injector.getInstance(HookSystem.class)));
		
		requestContext.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(injector.getInstance(MarkdownRenderer.class)));
		requestContext.add(TemplateEngineFeature.class, new TemplateEngineFeature(injector.getInstance(TemplateEngine.class)));
		
		return requestContext;
	}
	
	public void initContext (RequestContext requestContext, Request request) throws Exception {
		var uri = RequestUtil.getContentPath(request);
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		var theme = requestContext.get(ThemeFeature.class).theme();
		
		requestContext.add(RequestFeature.class, new RequestFeature(request.getContext().getContextPath(), uri, queryParameters, request));
		if (ServerContext.IS_DEV) {
			if (queryParameters.containsKey("preview")) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature());
			}
		}
		
		var markdownRenderer = injector.getInstance(MarkdownRenderer.class);
		var extensionManager = injector.getInstance(ExtensionManager.class);
		
		initHookSystem(requestContext);

		RequestExtensions requestExtensions = extensionManager.newContext(theme, requestContext);

		RenderContext renderContext = new RenderContext(
				markdownRenderer,
				initShortCodes(requestContext),
				theme);
		requestContext.add(RenderContext.class, renderContext);

		requestContext.add(RequestExtensions.class, requestExtensions);
	}
	
	private void initHookSystem(RequestContext requestContext) {
		var hookSystem = requestContext.get(HookSystemFeature.class).hookSystem();
		var moduleManager = injector.getInstance(ModuleManager.class);
		moduleManager.extensions(HookSystemRegisterExtensionPoint.class).forEach(extensionPoint -> extensionPoint.register(hookSystem));
	}
	
	/**
	 * Has to run as one of the last steps, because we need the requestContext
	 * to be filled
	 *
	 * @param requestContext
	 * @return
	 */
	private HookSystem setupAndGetHookSystem() {
		var hookSystem = injector.getInstance(HookSystem.class);
		var moduleManager = injector.getInstance(ModuleManager.class);
		moduleManager.extensions(HookSystemRegisterExtensionPoint.class).forEach(extensionPoint -> extensionPoint.register(hookSystem));

		return hookSystem;
	}

	private ShortCodes initShortCodes(RequestContext requestContext) {
		var parser = injector.getInstance(TagParser.class);

		var builder = ShortCodes.builder(parser);
		
		injector.getInstance(ModuleManager.class).extensions(RegisterShortCodesExtensionPoint.class)
				.forEach(extension -> {
					builder.register(extension.shortCodes());
					
					builder.register(extension.shortCodeDefinitions());
				});

		var codes = new HashMap<String, Function<Parameter, String>>();
		var wrapper = requestContext.get(ContentHooks.class).getShortCodes(codes);
		
		builder.register(wrapper.getShortCodes());

		return builder.build();
	}
	
	public RequestContext create() throws IOException {

		var theme = injector.getInstance(Theme.class);
		var markdownRenderer = injector.getInstance(MarkdownRenderer.class);
		var extensionManager = injector.getInstance(ExtensionManager.class);
		var siteProperties = injector.getInstance(SiteProperties.class);
		var siteMediaService = injector.getInstance(MediaService.class);

		var requestContext = new RequestContext();
		requestContext.add(InjectorFeature.class, new InjectorFeature(injector));

		requestContext.add(ThemeFeature.class, new ThemeFeature(theme));
		requestContext.add(ContentParserFeature.class, new ContentParserFeature(injector.getInstance(ContentParser.class)));
		requestContext.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(injector.getInstance(ContentNodeMapper.class)));
		if (ServerContext.IS_DEV) {
			requestContext.add(IsDevModeFeature.class, new IsDevModeFeature());
		}
		requestContext.add(ConfigurationFeature.class, new ConfigurationFeature(injector.getInstance(Configuration.class)));
		requestContext.add(ServerPropertiesFeature.class, new ServerPropertiesFeature(
				injector.getInstance(Configuration.class)
						.get(ServerConfiguration.class).serverProperties()
		));
		requestContext.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
		requestContext.add(SiteMediaServiceFeature.class, new SiteMediaServiceFeature(siteMediaService));

		requestContext.add(ServerHooks.class, new ServerHooks(requestContext));
		requestContext.add(TemplateHooks.class, new TemplateHooks(requestContext));
		requestContext.add(DBHooks.class, new DBHooks(requestContext));
		requestContext.add(ContentHooks.class, new ContentHooks(requestContext));

		requestContext.add(HookSystemFeature.class, new HookSystemFeature(setupAndGetHookSystem()));

		RequestExtensions requestExtensions = extensionManager.newContext(theme, requestContext);

		RenderContext renderContext = new RenderContext(
				markdownRenderer,
				initShortCodes(requestContext),
				theme);
		requestContext.add(RenderContext.class, renderContext);
		requestContext.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(markdownRenderer));
		requestContext.add(TemplateEngineFeature.class, new TemplateEngineFeature(injector.getInstance(TemplateEngine.class)));

		requestContext.add(RequestExtensions.class, requestExtensions);

		return requestContext;
	}

	// used
	public RequestContext create(
			String contextPath,
			String uri, Map<String, List<String>> queryParameters) throws IOException {
		return create(contextPath, uri, queryParameters, Optional.empty());
	}

	// used
	public RequestContext create(
			String contextPath,
			String uri, Map<String, List<String>> queryParameters, Optional<Request> request) throws IOException {

		var requestContext = create();

		requestContext.add(RequestFeature.class, new RequestFeature(contextPath, uri, queryParameters, request.orElse(null)));
		if (ServerContext.IS_DEV) {
			if (queryParameters.containsKey("preview")) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature());
			}
		}

		return requestContext;
	}
}
