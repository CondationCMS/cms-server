package com.github.thmarx.cms.server.jetty;

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
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.ThemeProperties;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.server.jetty.handler.JettyDefaultHandler;
import com.github.thmarx.cms.server.jetty.handler.JettyExtensionHandler;
import com.github.thmarx.cms.server.VHost;
import com.github.thmarx.cms.server.jetty.handler.JettyMediaHandler;
import com.github.thmarx.cms.server.jetty.handler.JettyModuleMappingHandler;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 *
 * @author t.marx
 */
@Slf4j
public class JettyVHost extends VHost {

	public JettyVHost(Path hostBase, ServerProperties serverProperties) {
		super(hostBase, serverProperties);
	}

	public Handler httpHandler() {
		
		var defaultHandler = new JettyDefaultHandler(contentResolver, requestContextFactory);

		log.debug("create assets handler for {}", assetBase.toString());
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(assetBase));
		if (serverProperties.dev()) {
			assetsHandler.setCacheControl("no-cache");
		} else {
			assetsHandler.setCacheControl("max-age=" + TimeUnit.HOURS.toSeconds(24));
		}

		ResourceHandler faviconHandler = new ResourceHandler();
		faviconHandler.setDirAllowed(false);
		faviconHandler.setBaseResource(new FileFolderPathResource(assetBase.resolve("favicon.ico")));

		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(PathSpec.from("/"), defaultHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/favicon.ico"), faviconHandler);
		
		JettyMediaHandler mediaHandler = new JettyMediaHandler(assetBase, mergedMediaFormats());
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);

		ContextHandler defaultContextHandler = new ContextHandler(pathMappingsHandler, "/");
		defaultContextHandler.setVirtualHosts(List.of(siteProperties.hostname()));

		var moduleHandler = new JettyModuleMappingHandler(moduleManager, siteProperties);
		moduleHandler.init();
		ContextHandler moduleContextHandler = new ContextHandler(moduleHandler, "/module");
		var extensionHandler = new JettyExtensionHandler(requestContextFactory);
		ContextHandler extensionContextHandler = new ContextHandler(extensionHandler, "/extension");

		ContextHandlerCollection contextCollection = new ContextHandlerCollection(
				defaultContextHandler,
				moduleContextHandler,
				extensionContextHandler
		);
		
		if (!getTheme().empty()) {
			contextCollection.addHandler(themeContextHandler());
		}

		GzipHandler gzipHandler = new GzipHandler(contextCollection);
		gzipHandler.setMinGzipSize(1024);
		gzipHandler.addIncludedMimeTypes("text/plain");
		gzipHandler.addIncludedMimeTypes("text/html");
		gzipHandler.addIncludedMimeTypes("text/css");
		gzipHandler.addIncludedMimeTypes("application/javascript");

		return gzipHandler;
	}
	
	private ContextHandler themeContextHandler () {
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(getTheme().assetsPath()));
		if (serverProperties.dev()) {
			assetsHandler.setCacheControl("no-cache");
		} else {
			assetsHandler.setCacheControl("max-age=" + TimeUnit.HOURS.toSeconds(24));
		}
		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		
		JettyMediaHandler mediaHandler = new JettyMediaHandler(getTheme().assetsPath(), mergedMediaFormats());
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);
		
		return new ContextHandler(pathMappingsHandler, "/themes/" + getTheme().getName());
	}
	
	private Map<String, ThemeProperties.MediaFormat> mergedMediaFormats () {
		Map<String, ThemeProperties.MediaFormat> formats = new HashMap<>();
		
		if (!getTheme().empty()) {
			formats.putAll(getTheme().properties().getMediaFormats());
		}
		formats.putAll(siteProperties.getMediaFormats());
		
		return formats;
	}
}
