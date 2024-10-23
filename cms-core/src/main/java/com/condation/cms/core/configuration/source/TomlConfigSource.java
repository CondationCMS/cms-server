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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlPosition;
import org.tomlj.TomlTable;

/**
 *
 * @author t.marx
 */
@Slf4j
public class TomlConfigSource implements ConfigSource {
	
	public static ConfigSource build(Path tomlfile) throws IOException {
		TomlTable result = null;
		if (Files.exists(tomlfile)) {
			result = Toml.parse(tomlfile);
		} else {
			result = EmptyTomlTable.EMPTY_TABLE;
		}

		return new TomlConfigSource(tomlfile, result);
	}

	private Map<String, Object> result;

	private final Path tomlFile;

	private long lastModified = 0;

	private TomlConfigSource(Path tomlFile, TomlTable result) {
		this.tomlFile = tomlFile;
		try {
			
			this.result = GSONProvider.GSON.fromJson(result.toJson(), HashMap.class);
			
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
			var toml = Toml.parse(tomlFile);
			
			this.result = GSONProvider.GSON.fromJson(toml.toJson(), HashMap.class);
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

	private static class EmptyTomlTable implements TomlTable {

		static final TomlTable EMPTY_TABLE = new EmptyTomlTable();

		private EmptyTomlTable() {
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}

		@Override
		public Set<List<String>> keyPathSet(boolean includeTables) {
			return Collections.emptySet();
		}

		@Override
		public Set<Map.Entry<String, Object>> entrySet() {
			return Collections.emptySet();
		}

		@Override
		public Set<Map.Entry<List<String>, Object>> entryPathSet(boolean includeTables) {
			return Collections.emptySet();
		}

		@Override
		public Object get(List<String> path) {
			return null;
		}

		@Override
		public TomlPosition inputPositionOf(List<String> path) {
			return null;
		}

		@Override
		public Map<String, Object> toMap() {
			return Collections.emptyMap();
		}
	}

}
