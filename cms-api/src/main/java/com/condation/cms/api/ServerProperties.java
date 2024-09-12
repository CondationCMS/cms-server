package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
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
		return !Constants.Environments.PROD.equals(env());
	}
	
	public String env () {
		if (System.getenv().containsKey("CMS_ENV")) {
			return System.getenv("CMS_ENV");
		}
		if (System.getProperties().containsKey("cms.env")) {
			return System.getProperty("cms.env");
		}
		return (String) properties.getOrDefault("env", Constants.Environments.PROD);
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
	
	public APMProperties apm () {
		return new APMProperties(getSubMap("apm"));
	}
	
	public IPCProperties ipc () {
		return new IPCProperties(getSubMap("ipc"));
	}
	
	public PerformanceProperties performance () {
		return new PerformanceProperties(getSubMap("performance"));
	}
}
