package com.condation.cms.core.utils;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;

public class EnvUtil {

    public static Map<String, Object> load(String path) {
        Map<String, Object> envMap = new HashMap<>();

        // Load .env file if it exists
        Dotenv dotenv = Dotenv.configure().directory(path).ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            addValueToMap(envMap, key, value);
        });

        // Load system environment variables, overriding .env variables
        System.getenv().forEach((key, value) -> {
            addValueToMap(envMap, key, value);
        });
        
        // Load system properties, overriding system environment variables
        System.getProperties().forEach((key, value) -> {
            addValueToMap(envMap, (String) key, (String) value);
        });

        return envMap;
    }

    private static void addValueToMap(Map<String, Object> map, String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        String[] parts = key.split("_");
        if (parts.length == 0) {
            return;
        }

        Map<String, Object> currentMap = map;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].toLowerCase();
            if (part.isEmpty()) {
                continue;
            }
            Object node = currentMap.get(part);
            if (node instanceof Map) {
                currentMap = (Map<String, Object>) node;
            } else if (node == null) {
                Map<String, Object> newMap = new HashMap<>();
                currentMap.put(part, newMap);
                currentMap = newMap;
            } else {
                // A value is already present, cannot create a map here.
                return;
            }
        }
        currentMap.put(parts[parts.length - 1].toLowerCase(), value);
    }
}
