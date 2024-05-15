package com.github.thmarx.cms.server.jetty;

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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.configuration.ConfigurationManagement;
import com.github.thmarx.cms.api.configuration.configs.ServerConfiguration;
import com.github.thmarx.cms.api.eventbus.Event;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.HostReadyEvent;
import com.github.thmarx.cms.api.eventbus.events.ServerReadyEvent;
import com.github.thmarx.cms.api.eventbus.events.ServerShutdownInitiated;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.thmarx.cms.server.VHost;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.QoSHandler;
import org.eclipse.jetty.server.handler.ThreadLimitHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyServer implements AutoCloseable {

	private final ServerProperties properties;
	private Server server;
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private EventBus serverEventBus = new DefaultEventBus();
	
	public void fireServerEvent (Event event) {
		serverEventBus.publish(event);
	}

	public void startup() throws IOException {
		
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		
		List<VHost> vhosts = new ArrayList<>();
		Files.list(Path.of("hosts")).forEach((hostPath) -> {
			var props = hostPath.resolve("site.yaml");
			if (Files.exists(props)) {
				try {
					Configuration configuration = new Configuration(hostPath);
					configuration.add(ServerConfiguration.class, new ServerConfiguration(properties));
					var host = new VHost(hostPath, configuration, scheduledExecutorService);
					host.init(Path.of(Constants.Folders.MODULES));
					vhosts.add(host);

					host.getInjector().getInstance(ConfigurationManagement.class).init();
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		ContextHandlerCollection handlers = new ContextHandlerCollection();
		vhosts.forEach(host -> {
			log.debug("add virtual host : " + host.hostnames());
			var httpHandler = host.httpHandler();
			handlers.addHandler(httpHandler);
		});

		serverEventBus.register(ServerShutdownInitiated.class, (event) -> {
			System.exit(0);
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("shutting down");

			vhosts.forEach(host -> {
				log.debug("shutting down vhost : " + host.hostnames());
				host.shutdown();
			});
			scheduledExecutorService.shutdownNow();
			
			log.debug("exit");
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
		connector.setPort(properties.serverPort());
		connector.setHost(properties.serverIp());

		server.addConnector(connector);

		var apm = properties.apm();
		if (apm.enabled()) {
			log.info("enable application performance management");
			ThreadLimitHandler threadLimitHandler = new ThreadLimitHandler(HttpHeader.X_FORWARDED_FOR.asString());
			threadLimitHandler.setThreadLimit(apm.thread_limit());
			threadLimitHandler.setHandler(handlers);
			
			QoSHandler qosHandler = new QoSHandler(threadLimitHandler);
			qosHandler.setMaxRequestCount(apm.max_requests());
			qosHandler.setMaxSuspend(apm.max_suspend());
			
			server.setHandler(qosHandler);
		} else {
			server.setHandler(handlers);
		}
		
		try {
			server.start();
			
			vhosts.forEach(host -> {
				host.getInjector().getInstance(EventBus.class).publish(new HostReadyEvent(host.id()));
				host.getInjector().getInstance(EventBus.class).publish(new ServerReadyEvent());
			});
			
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}

	@Override
	public void close() throws Exception {
		server.stop();
	}
}
