package com.github.thmarx.cms.server.undertow;

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

import com.github.thmarx.cms.server.undertow.handler.UndertowExtensionsHttpHandler;
import com.github.thmarx.cms.server.undertow.handler.UndertowDefaultHttpHandler;
import com.github.thmarx.cms.Startup;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.server.VHost;
import com.github.thmarx.cms.server.undertow.handler.UndertowModuleHttpHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author t.marx
 */
@Deprecated(since = "2.5.0")
public class UndertowVHost extends VHost {

	public UndertowVHost(Path hostBase, ServerProperties serverProperties) {
		super(hostBase, serverProperties);
	}
	public HttpHandler httpHandler() {
		final PathResourceManager resourceManager = new PathResourceManager(assetBase);
		ResourceHandler staticResourceHandler = new ResourceHandler(resourceManager);
		// TODO: think about some better strategy for ttl
		if (!Startup.DEV_MODE) {
			staticResourceHandler.setCacheTime((int)TimeUnit.DAYS.toSeconds(1));
		}
		HttpHandler compressionHandler = new EncodingHandler.Builder().build(null).wrap(staticResourceHandler);
		//DirectBufferCache assetCache = new DirectBufferCache(100, 10, 1000);
		//CacheHandler cacheHandler = new CacheHandler(assetCache, new EncodingHandler.Builder().build(null).wrap(staticResourceHandler));
		
		ResourceHandler faviconHandler = new ResourceHandler(new FileResourceManager(assetBase.resolve("favicon.ico").toFile()));
		
		var pathHandler = Handlers.path(new UndertowDefaultHttpHandler(contentResolver, extensionManager, (context) -> {
			return resolveMarkdownRenderer(context);
		}))
				.addPrefixPath("/assets", compressionHandler)
				.addExactPath("/favicon.ico", faviconHandler);

		RoutingHandler extensionHandler = Handlers.routing();
		extensionHandler.get("/{name}", new UndertowExtensionsHttpHandler(extensionManager, "get"));
		extensionHandler.post("/{name}", new UndertowExtensionsHttpHandler(extensionManager, "post"));
		pathHandler.addPrefixPath("/extension", extensionHandler);

		RoutingHandler moduleHandler = Handlers.routing();
		moduleHandler.get("/{module}/*", new UndertowModuleHttpHandler(moduleManager, "get"));
		moduleHandler.post("/{module}/*", new UndertowModuleHttpHandler(moduleManager, "post"));
		pathHandler.addPrefixPath("/module", moduleHandler);
		
		return pathHandler;
	}
}
