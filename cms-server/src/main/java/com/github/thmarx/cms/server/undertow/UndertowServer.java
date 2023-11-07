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

import com.github.thmarx.cms.api.ServerProperties;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.thmarx.cms.server.HttpServer;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "2.5.0")
public class UndertowServer implements HttpServer {

	private final ServerProperties properties;
	private Undertow server;
	
	@Override
	public void startup() throws IOException {
		List<UndertowVHost> vhosts = new ArrayList<>();
		Files.list(Path.of("hosts")).forEach((hostPath) -> {
			var props = hostPath.resolve("site.yaml");
			if (Files.exists(props)) {
				try {
					UndertowVHost host = new UndertowVHost(hostPath, properties);
					host.init(Path.of("modules"));
					vhosts.add(host);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		var hostHandlers = Handlers.virtualHost();
		vhosts.forEach(host -> {
			log.debug("add virtual host : " + host.getHostname());
			hostHandlers.addHost(host.getHostname(), host.httpHandler());
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("shutting down");

			vhosts.forEach(host -> {
				log.debug("shutting down vhost : " + host.getHostname());
				host.shutdown();
			});
		}));

		server = Undertow.builder()
				.addHttpListener(
						properties.serverPort(), 
						properties.serverIp()
				).setHandler(hostHandlers)
				.setServerOption(UndertowOptions.URL_CHARSET, "UTF8")
				.build();
		server.start();
	}

	@Override
	public void close() throws Exception {
		server.stop();
	}

}
