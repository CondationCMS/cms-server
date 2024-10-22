package com.condation.cms.core.configuration.properties;

/*-
 * #%L
 * cms-core
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

import com.condation.cms.api.APMProperties;
import com.condation.cms.api.Constants;
import com.condation.cms.api.IPCProperties;
import com.condation.cms.api.PerformanceProperties;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import java.nio.file.Path;

/**
 *
 * @author t.marx
 */
public class ExtendedServerProperties implements ServerProperties {
	
	private final SimpleConfiguration configuration;
	
	public ExtendedServerProperties(SimpleConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public PerformanceProperties performance() {
		return configuration.get("performance", ExtendedPerformanceProperties.class);
	}

	@Override
	public IPCProperties ipc() {
		return configuration.get("ipc", ExtendedIPCProperties.class);
	}

	@Override
	public APMProperties apm() {
		return configuration.get("apm", ExtendedAPMProperties.class);
	}

	@Override
	public Path getThemesFolder() {
		return Path.of("themes/");
	}

	@Override
	public int serverPort() {
		return configuration.getInteger("server.port", 8080);
	}

	@Override
	public String serverIp() {
		return configuration.getString("server.ip", "127.0.0.1");
	}

	@Override
	public String env() {
		if (System.getenv().containsKey("CMS_ENV")) {
			return System.getenv("CMS_ENV");
		}
		if (System.getProperties().containsKey("cms.env")) {
			return System.getProperty("cms.env");
		}
		return configuration.getString("env", Constants.Environments.PROD);
	}

	@Override
	public boolean dev() {
		return !Constants.Environments.PROD.equals(env());
	}
	
	
	
}
