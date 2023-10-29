package com.github.thmarx.cms;

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
import com.github.thmarx.cms.server.HttpServer;
import com.github.thmarx.cms.server.jetty.JettyServer;
import com.github.thmarx.cms.server.undertow.UndertowServer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class Startup {

	public static boolean DEV_MODE = false;
	
	public static void main(String[] args) throws Exception {

		printStartup();
		
		System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
		System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");

		ServerProperties properties = PropertiesLoader.serverProperties(Path.of("server.yaml"));
		
		DEV_MODE = properties.dev();

		var server = getServerEngine(properties);
		server.startup();
	}
	
	private static HttpServer getServerEngine (ServerProperties properties) {
		var engine = properties.serverEngine();
		log.debug("try to load engine: {}", engine);
		return switch (engine) {
			case "jetty" -> new JettyServer(properties);
			case "undertow" -> new UndertowServer(properties);
			default -> throw new RuntimeException("something bad happens");
		};
	}
	
	private static void printStartup () throws IOException {
		try (var in = Startup.class.getResourceAsStream("application.properties")) {
			Properties props = new Properties();
			props.load(in);
			
			log.info("starting {} version {}", props.getProperty("name"), props.getProperty("version"));
			log.info("build {}", props.getProperty("build.date"));
		}
	}

}