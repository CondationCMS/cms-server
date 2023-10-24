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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.thmarx.cms.server.HttpServer;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.strategy.AdaptiveExecutionStrategy;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyServer implements HttpServer {

	private final Properties properties;
	private Server server;

	@Override
	public void startup() throws IOException {
		List<JettyVHost> vhosts = new ArrayList<>();
		Files.list(Path.of("hosts")).forEach((hostPath) -> {
			var props = hostPath.resolve("site.yaml");
			if (Files.exists(props)) {
				try {
					var host = new JettyVHost(hostPath);
					host.init();
					vhosts.add(host);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		vhosts.forEach(host -> {
			log.debug("add virtual host : " + host.getHostname());
			var httpHandler = host.httpHandler();
			handlers.addHandler(httpHandler);
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("shutting down");

			vhosts.forEach(host -> {
				log.debug("shutting down vhost : " + host.getHostname());
				host.shutdown();
			});
		}));

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSendServerVersion(false);
		httpConfig.setSendXPoweredBy(false);
		HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setVirtualThreadsExecutor(Executors.newVirtualThreadPerTaskExecutor());

		server = new Server(threadPool);
		server.setRequestLog(new CustomRequestLog(new Slf4jRequestLogWriter(), CustomRequestLog.EXTENDED_NCSA_FORMAT));

		ServerConnector connector = new ServerConnector(server, http11);
		connector.setPort(Integer.valueOf(properties.getProperty("server.port", "8080")));
		connector.setHost(properties.getProperty("server.ip", "127.0.0.1"));

		server.addConnector(connector);
		
		
		server.setHandler(handlers);
		try {
			server.start();		
		} catch (Exception ex) {
			Logger.getLogger(JettyServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void close() throws Exception {
		server.stop();
	}

}
