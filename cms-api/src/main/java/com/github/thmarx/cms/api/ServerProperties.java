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

import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ServerProperties {
	
	private final Map<String, Object> properties;
	
	public boolean dev () {
		return (Boolean) properties.getOrDefault("dev", true);
	}
	
	private Map<String, Object> getSubMap (final String name) {
		return (Map<String, Object>) properties.getOrDefault(name, Collections.emptyMap());
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
}
