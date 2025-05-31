package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MetaConverter {

    public static Map<String, Object> convertMeta(Map<String, Object> rawMeta) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawMeta.entrySet()) {
            String key = entry.getKey();
            Object valueObj = entry.getValue();

            if (!(valueObj instanceof Map)) {
                result.put(key, valueObj);
                continue;
            }

            Map<?, ?> field = (Map<?, ?>) valueObj;
            Object typeObj = field.get("type");
            Object valObj = field.get("value");

            if (!(typeObj instanceof String) || valObj == null) {
                result.put(key, valObj);
                continue;
            }

            String type = ((String) typeObj).toLowerCase();
            String valueStr = valObj.toString();

            try {
                switch (type) {
                    case "date":
                    case "datetime":
                        // Erwartet vollständigen ISO-8601 String mit Z, z. B. 2025-05-31T13:30:00Z
                        Instant instant = Instant.parse(valueStr);
                        result.put(key, Date.from(instant));
                        break;
                    default:
                        result.put(key, valObj);
                        break;
                }
            } catch (Exception e) {
                // Falls Parsing fehlschlägt → originaler Wert
                result.put(key, valObj);
            }
        }

        return result;
    }
}
