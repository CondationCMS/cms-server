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
import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.server.jetty.handler.JettyDefaultHandler;
import com.github.thmarx.cms.server.jetty.handler.JettyModuleHandler;
import com.github.thmarx.cms.server.jetty.handler.JettyExtensionHandler;
import com.github.thmarx.cms.server.VHost;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
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
		var defaultHandler = new JettyDefaultHandler(contentResolver, extensionManager, (context) -> {
			return resolveMarkdownRenderer(context);
		});
	
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
		
		ContextHandler defaultContextHandler = new ContextHandler(pathMappingsHandler, "/");
		defaultContextHandler.setVirtualHosts(List.of(properties.hostname()));
		
		
//		var moduleHandler = new JettyModuleHandler(moduleManager);
//		ContextHandler moduleContextHandler = new ContextHandler(moduleHandler, "/module");
		var extensionHandler = new JettyExtensionHandler(extensionManager);
		ContextHandler extensionContextHandler = new ContextHandler(extensionHandler, "/extension");
		
		ContextHandlerCollection contextCollection = new ContextHandlerCollection(
				defaultContextHandler,
//				moduleContextHandler,
				extensionContextHandler,
				modulesContextHandler()
		);
		
		
		GzipHandler gzipHandler = new GzipHandler(contextCollection);
		gzipHandler.setMinGzipSize(1024);
		gzipHandler.addIncludedMimeTypes("text/plain");
        gzipHandler.addIncludedMimeTypes("text/html");
		gzipHandler.addIncludedMimeTypes("text/css");
		gzipHandler.addIncludedMimeTypes("application/javascript");

		return gzipHandler;
	}
	
	private ContextHandler modulesContextHandler () {
		List<ContextHandler> contextHandlers = new ArrayList<>();
		moduleManager.extensions(JettyHttpHandlerExtensionPoint.class).forEach((extension) -> {
			contextHandlers.add(new ContextHandler(
					extension.getHandler(), "/module/" + extension.getContextPath() 
			));
		});
		ContextHandler modulesContextHandler = new ContextHandler(
				new ContextHandlerCollection(contextHandlers.toArray(ContextHandler[]::new)),
				"/module"
		);
		
		return modulesContextHandler;
	}
}
