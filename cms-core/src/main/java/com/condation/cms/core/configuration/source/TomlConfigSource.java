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
import com.condation.cms.core.configuration.GSONProvider;
import com.google.gson.JsonObject;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class TomlConfigSource implements ConfigSource {
	
	private static JToml JTOML = JToml.jToml();
	
	public static ConfigSource build(Path tomlfile) throws IOException {
		
		TomlDocument document = null;
		if (Files.exists(tomlfile)) {
			document = JTOML.read(tomlfile);
		} else {
			document = JTOML.readFromString("");
		}
		
		return new TomlConfigSource(tomlfile, document);
	}

	private Map<String, Object> result;

	private final Path tomlFile;

	private long lastModified = 0;

	private TomlConfigSource(Path tomlFile, TomlDocument document) {
		this.tomlFile = tomlFile;
		try {
			
			var json = JTOML.fromToml(JsonObject.class, document);
			this.result = GSONProvider.GSON.fromJson(
					GSONProvider.GSON.toJson(json), 
					HashMap.class);
			
			if (Files.exists(tomlFile)) {
				this.lastModified = Files.getLastModifiedTime(tomlFile).toMillis();
			}
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	@Override
	public boolean reload() {
		if (!Files.exists(tomlFile)) {
			return false;
		}
		try {

			var modified = Files.getLastModifiedTime(tomlFile).toMillis();
			if (modified <= lastModified) {
				return false;
			}
			lastModified = modified;
			var document = JTOML.read(tomlFile);
			
			var json = JTOML.fromToml(JsonObject.class, document);
			this.result = GSONProvider.GSON.fromJson(
					GSONProvider.GSON.toJson(json), 
					HashMap.class);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return true;
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
		return Files.exists(tomlFile);
	}
}