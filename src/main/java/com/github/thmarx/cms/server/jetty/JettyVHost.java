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
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
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
		final PathResourceFactory pathResourceFactory = new PathResourceFactory();
		
		var defaultHandler = new JettyDefaultHandler(contentResolver, extensionManager, (context) -> {
			return resolveMarkdownRenderer(context);
		});
	
		log.debug("create assets handler for {}", assetBase.toString());
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(assetBase));
		
		ResourceHandler faviconHandler = new ResourceHandler();
		faviconHandler.setDirAllowed(false);
		faviconHandler.setBaseResource(new FileFolderPathResource(assetBase.resolve("favicon.ico")));
		
		var extensionHandler = new JettyExtensionHandler(extensionManager);
		
		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(PathSpec.from("/"), defaultHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
        pathMappingsHandler.addMapping(PathSpec.from("/favicon.ico"), faviconHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/extensions/*"), extensionHandler);
		
		ContextHandler contextHandler = new ContextHandler(pathMappingsHandler, "/");
		contextHandler.setVirtualHosts(List.of(properties.hostname()));
		
		GzipHandler gzipHandler = new GzipHandler(contextHandler);
		gzipHandler.setMinGzipSize(10);
		gzipHandler.addIncludedMimeTypes("text/plain");
        gzipHandler.addIncludedMimeTypes("text/html");
		gzipHandler.addIncludedMimeTypes("text/css");
		gzipHandler.addIncludedMimeTypes("application/javascript");

		return gzipHandler;
	}
}
