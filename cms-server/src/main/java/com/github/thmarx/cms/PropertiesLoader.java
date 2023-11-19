package com.github.thmarx.cms;

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

import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.ServerProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public abstract class PropertiesLoader {
	
	public static SiteProperties hostProperties (Path path) throws IOException {
		Map<String, Object> properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		return new SiteProperties(properties);
	}
	
	public static ServerProperties serverProperties (Path path) throws IOException {
		Map<String, Object> properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		return new ServerProperties(properties);
	}
}
