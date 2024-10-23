package com.condation.cms.core.configuration.source;

/*-
 * #%L
 * tests
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

import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.core.configuration.ConfigSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class YamlConfigSource implements ConfigSource {
	
	public static ConfigSource build (Path yamlFile) throws IOException {
		Map<String, Object> result = null;
		if (Files.exists(yamlFile)) {
			result = (Map<String, Object>) new Yaml().load(Files.newBufferedReader(yamlFile, StandardCharsets.UTF_8));
		} else {
			result = Collections.emptyMap();
		}
		
		return new YamlConfigSource(yamlFile, result);
	}
	
	private Map<String, Object> result;
	private final Path configFile;
	
	private long lastModified;
	
	private YamlConfigSource (Path configFile, Map<String, Object> result) {
		this.result = result;
		this.configFile = configFile;
		
		try {
			if (Files.exists(configFile)) {
				this.lastModified = Files.getLastModifiedTime(configFile).toMillis();
			}
		} catch (IOException ioe) {
			log.error("", ioe);
		}
	}

	@Override
	public boolean reload() {
		if (!Files.exists(configFile)) {
			return false;
		}
		try {
			
			var modified = Files.getLastModifiedTime(configFile).toMillis();
			if (modified <= lastModified) {
				return false;
			}
			lastModified = modified;
			
			result = (Map<String, Object>) new Yaml().load(Files.newBufferedReader(configFile, StandardCharsets.UTF_8));
			return true;
		} catch (IOException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}
	
	

	@Override
	public String getString(String field) {
		return (String)MapUtil.getValue(result, field);
	}
	@Override
	public Object get(String field) {
		return MapUtil.getValue(result, field);
	}

	@Override
	public Map<String, Object> getMap(String field) {
		return MapUtil.getValue(result, field, Collections.emptyMap());
	}
	@Override
	public List<Object> getList(String field) {
		return MapUtil.getValue(result, field, Collections.emptyList());
	}

	@Override
	public boolean exists() {
		return Files.exists(configFile);
	}
}
