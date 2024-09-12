package com.condation.cms.server;

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


import com.condation.cms.api.Constants;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.ConfigurationManagement;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.lifecycle.HostReadyEvent;
import com.condation.cms.api.eventbus.events.lifecycle.ReloadHostEvent;
import com.condation.cms.api.eventbus.events.lifecycle.ServerReadyEvent;
import com.condation.cms.api.eventbus.events.lifecycle.ServerShutdownInitiated;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.server.VHost;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.graalvm.polyglot.Engine;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyServer implements AutoCloseable {

	private final Injector globalInjector;
	private Server server;

	private final EventBus serverEventBus = new DefaultEventBus();

	List<VHost> vhosts = new ArrayList<>();
	ContextHandlerCollection handlerCollection = new ContextHandlerCollection();

	public void fireServerEvent(Event event) {
		serverEventBus.publish(event);
	}
	
	public void reloadVHost(String vhost) {
		log.debug("try reloading " + vhost);
		vhosts.stream()
				.filter(host -> host.id().equals(vhost))
				.forEach(host -> {
					try {
						host.reload();
					} catch (Exception e) {
						log.error("", e);
					}
				});
	}

	public void startup() throws IOException {

//		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		var properties = globalInjector.getInstance(ServerProperties.class);

		Files.list(Path.of("hosts")).forEach((hostPath) -> {
			var props = hostPath.resolve("site.yaml");
			if (Files.exists(props)) {
				try {
					Configuration configuration = new Configuration(hostPath);
					configuration.add(ServerConfiguration.class, new ServerConfiguration(properties));
					var host = new VHost(hostPath, configuration);
					host.init(Path.of(Constants.Folders.MODULES), globalInjector);
					vhosts.add(host);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		vhosts.forEach(host -> {
			log.info("add virtual host : " + host.hostnames());
			var httpHandler = host.buildHttpHandler();
			handlerCollection.addHandler(httpHandler);
		});

		serverEventBus.register(ServerShutdownInitiated.class, (event) -> {
			System.exit(0);
		});

		serverEventBus.register(ReloadHostEvent.class, (event) -> {
			reloadVHost(event.host());
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("shutting down");

			vhosts.forEach(host -> {
				log.debug("shutting down vhost : " + host.hostnames());
				host.shutdown();
			});
//			scheduledExecutorService.shutdownNow();
			
			try {
				globalInjector.getInstance(Scheduler.class).shutdown();
			} catch (SchedulerException ex) {
				log.error("", ex);
			}
			globalInjector.getInstance(Engine.class).close(true);

			log.debug("exit");
		}));

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSendServerVersion(false);
		httpConfig.setSendXPoweredBy(false);
		HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

		QueuedThreadPool threadPool = new QueuedThreadPool(properties.performance().request_workers());
		threadPool.setName("cms-request-worker");
		//threadPool.setVirtualThreadsExecutor(Executors.newVirtualThreadPerTaskExecutor());

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
			threadLimitHandler.setHandler(handlerCollection);

			QoSHandler qosHandler = new QoSHandler(threadLimitHandler);
			qosHandler.setMaxRequestCount(apm.max_requests());
			qosHandler.setMaxSuspend(apm.max_suspend());

			server.setHandler(qosHandler);
		} else {
			server.setHandler(handlerCollection);
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
		System.out.println("cms startup successfully");
	}

	@Override
	public void close() throws Exception {
		server.stop();
	}
}
