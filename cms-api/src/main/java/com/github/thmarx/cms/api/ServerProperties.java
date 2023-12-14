package com.github.thmarx.cms.api;

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
