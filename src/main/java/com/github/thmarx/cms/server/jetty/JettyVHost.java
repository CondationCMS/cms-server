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

import com.github.thmarx.cms.server.VHost;
import com.github.thmarx.cms.utils.PathUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.PathResourceFactory;

/**
 *
 * @author t.marx
 */
@Slf4j
public class JettyVHost extends VHost {

	public JettyVHost(Path hostBase) {
		super(hostBase);
	}

	public Handler httpHandler() {
		ContextHandlerCollection contextCollection = new ContextHandlerCollection();
		final PathResourceFactory pathResourceFactory = new PathResourceFactory();

		// default handler
		
		var defaultHandler = new JettyDefaultHandler(contentResolver, extensionManager, (context) -> {
			return resolveMarkdownRenderer(context);
		});
		final ContextHandler defaultContextHandler = new ContextHandler("/");
		defaultContextHandler.setContextPath("/");
		defaultContextHandler.setHandler(defaultHandler);
		defaultContextHandler.setVirtualHosts(List.of(properties.hostname()));
		contextCollection.addHandler(defaultContextHandler);
	
		log.debug("create assets handler for {}", assetBase.toString());
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(pathResourceFactory.newResource(assetBase));
		final ContextHandler assetsContextHandler = new ContextHandler(assetsHandler, "/assets");
		assetsContextHandler.addAliasCheck((eins, zwei) -> {
			try {
				return PathUtil.isChild(assetBase, zwei.getPath());
			} catch (IOException ioe) {
				log.error(null,ioe);
			}
			return false;
		});
		assetsContextHandler.setVirtualHosts(List.of(properties.hostname()));
		contextCollection.addHandler(assetsContextHandler);
		
		ResourceHandler faviconHandler = new ResourceHandler();
		faviconHandler.setDirAllowed(false);
		faviconHandler.setBaseResource(pathResourceFactory.newResource(assetBase.resolve("favicon.ico")));
		final ContextHandler faviconContextHandler = new ContextHandler(faviconHandler, "/favicon.ico");
		faviconContextHandler.addAliasCheck((eins, zwei) -> {
			try {
				return PathUtil.isChild(assetBase, zwei.getPath());
			} catch (IOException ioe) {
				log.error(null,ioe);
			}
			return false;
		});
		faviconContextHandler.setVirtualHosts(List.of(properties.hostname()));
		contextCollection.addHandler(faviconContextHandler);
		
		var extensionHandler = new JettyExtensionHandler(extensionManager);
		final ContextHandler extensionContextHandler = new ContextHandler(extensionHandler, "/extensions");
		extensionContextHandler.setVirtualHosts(List.of(properties.hostname()));
		contextCollection.addHandler(extensionContextHandler);
		
		
		GzipHandler gzipHandler = new GzipHandler(contextCollection);
		gzipHandler.setMinGzipSize(10);
		gzipHandler.addIncludedMimeTypes("text/plain");
        gzipHandler.addIncludedMimeTypes("text/html");
		gzipHandler.addIncludedMimeTypes("text/css");
		gzipHandler.addIncludedMimeTypes("application/javascript");

		return gzipHandler;
	}
}
