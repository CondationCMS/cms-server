package com.condation.cms.core.content.io;

/*-
 * #%L
 * CMS Core
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

import java.util.*;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class YamlHeaderUpdater {

	/**
	 * Inserts flat keys like "seo.title" into a nested map structure.
	 *
	 * @param targetMap The existing map (e.g., the YAML header)
	 * @param flatMap A flat map using dot notation ("seo.title" → "...")
	 */
	@SuppressWarnings("unchecked")
	public static void mergeFlatMapIntoNestedMap(Map<String, Object> targetMap, Map<String, Object> flatMap) {
		for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
			String[] keys = entry.getKey().split("\\.");
			Map<String, Object> current = targetMap;

			for (int i = 0; i < keys.length - 1; i++) {
				String key = keys[i];

				Object next = current.get(key);
				if (next instanceof Map) {
					current = (Map<String, Object>) next;
				} else if (next == null) {
					Map<String, Object> newMap = new LinkedHashMap<>();
					current.put(key, newMap);
					current = newMap;
				} else {
					// Konflikt: vorhandener Wert ist kein Map → überschreiben
					Map<String, Object> newMap = new LinkedHashMap<>();
					current.put(key, newMap);
					current = newMap;
				}
			}

			// Letzter Key → Wert setzen
			current.put(keys[keys.length - 1], entry.getValue());
		}
	}
	
	    /**
     * Saves the metadata as a YAML front matter and appends the Markdown content.
     *
     * @param filePath Path to the Markdown file to write to
     * @param metadata Map containing the nested metadata (YAML structure)
     * @param content  The Markdown content (body)
     * @throws IOException if writing fails
     */
    public static void saveMarkdownFileWithHeader(Path filePath, Map<String, Object> metadata, String content) throws IOException {
        // Configure pretty YAML output
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(metadata);

        // Build full file content
        StringBuilder builder = new StringBuilder();
        builder.append("---\n");
        builder.append(yamlContent);
        builder.append("---\n\n");
        builder.append(content.trim()).append("\n");

		writeAtomically(filePath, builder.toString());
    }
	
	public static void saveMetaData(Path filePath, Map<String, Object> metadata) throws IOException {
        // Configure pretty YAML output
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(metadata);

        // Build full file content
        StringBuilder builder = new StringBuilder();
        builder.append(yamlContent);

        // Write to file
		writeAtomically(filePath, builder.toString());
    }

	private static void writeAtomically(Path filePath, String content) throws IOException {
		var absolutePath = filePath.toAbsolutePath();
		var parent = absolutePath.getParent();
		var temporaryFile = Files.createTempFile(
				parent, "." + absolutePath.getFileName() + ".cms-write-", ".tmp");
		try {
			Files.writeString(temporaryFile, content, StandardCharsets.UTF_8);
			try {
				Files.move(temporaryFile, absolutePath,
						StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporaryFile);
		}
	}
}
