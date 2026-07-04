package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ui.rpc.RPCException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class TaxonomyYamlWriter {

	public void writeValue(Path hostBase, String taxonomySlug, String id, String title) throws Exception {
		var configDir = hostBase.resolve("config").normalize();
		var targetFile = configDir.resolve("taxonomy.%s.yaml".formatted(taxonomySlug)).normalize();

		if (!targetFile.startsWith(configDir)) {
			throw new RPCException(1, "invalid taxonomy path");
		}

		Files.createDirectories(configDir);

		Map<String, Object> document = loadTaxonomyValuesDocument(targetFile);
		List<Map<String, String>> values = getTaxonomyValues(document);

		var existsInFile = values.stream()
				.anyMatch(value -> id.equals(value.get("id")));
		if (!existsInFile) {
			Map<String, String> newValue = new LinkedHashMap<>();
			newValue.put("id", id);
			newValue.put("title", title);
			values.add(newValue);
		}

		document.put("values", values);
		Files.writeString(targetFile, createYaml().dump(document), StandardCharsets.UTF_8);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> loadTaxonomyValuesDocument(Path targetFile) throws Exception {
		if (!Files.exists(targetFile) || Files.readString(targetFile, StandardCharsets.UTF_8).isBlank()) {
			return new LinkedHashMap<>();
		}

		var loaded = createYaml().load(Files.readString(targetFile, StandardCharsets.UTF_8));
		if (loaded == null) {
			return new LinkedHashMap<>();
		}
		if (!(loaded instanceof Map<?, ?> map)) {
			throw new RPCException(1, "invalid taxonomy document");
		}
		return new LinkedHashMap<>((Map<String, Object>) map);
	}

	private List<Map<String, String>> getTaxonomyValues(Map<String, Object> document) throws RPCException {
		var loadedValues = document.get("values");
		if (loadedValues == null) {
			return new ArrayList<>();
		}
		if (!(loadedValues instanceof List<?> rawValues)) {
			throw new RPCException(1, "invalid taxonomy values");
		}

		List<Map<String, String>> values = new ArrayList<>();
		for (Object rawValue : rawValues) {
			if (!(rawValue instanceof Map<?, ?> value)) {
				throw new RPCException(1, "invalid taxonomy value");
			}
			Map<String, String> taxonomyValue = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				taxonomyValue.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
			}
			values.add(taxonomyValue);
		}
		return values;
	}

	private Yaml createYaml() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		options.setExplicitStart(true);
		return new Yaml(options);
	}
}
