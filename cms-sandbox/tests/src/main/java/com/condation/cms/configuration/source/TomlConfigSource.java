package com.condation.cms.configuration.source;

import com.condation.cms.configuration.ConfigSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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

	private TomlTable result;

	private final Path tomlFile;
	
	private long lastModified = 0;

	private TomlConfigSource(Path tomlFile, TomlTable result) {
		this.tomlFile = tomlFile;
		this.result = result;
		try {
			this.lastModified = Files.getLastModifiedTime(tomlFile).toMillis();
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
			this.result = Toml.parse(tomlFile);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	@Override
	public String getString(String field) {
		return result.getString(field);
	}
	@Override
	public Object get(String field) {
		return result.get(field);
	}

	@Override
	public Map<String, Object> getMap(String field) {
		return result.getTableOrEmpty(field).toMap();
	}

	@Override
	public List<Object> getList(String field) {
		return result.getArrayOrEmpty(field).toList()
				.stream()
				.map(item -> {
					if (item instanceof TomlTable table) {
						return table.toMap();
					} else if (item instanceof TomlArray array) {
						return array.toList();
					} else {
						return item;
					}
				}).collect(Collectors.toList());
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
