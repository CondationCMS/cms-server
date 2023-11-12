package com.github.thmarx.cms.api;

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

import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class ServerProperties extends YamlProperties {
	
	public ServerProperties (final Map<String, Object> properties) {
		super(properties);
	}
	
	public boolean dev () {
		return (Boolean) properties.getOrDefault("dev", true);
	}
	
	public String serverEngine () {
		return (String)getSubMap("server").getOrDefault("engine", "jetty");
	}
	public String serverIp () {
		return (String)getSubMap("server").getOrDefault("ip", "127.0.0.1");
	}
	public int serverPort () {
		return (int)getSubMap("server").getOrDefault("port", 8080);
	}
	
	public Path getThemesFolder () {
		return Path.of("themes/");
	}
}
