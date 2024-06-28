package com.github.thmarx.cms.filesystem.metadata.persistent.utils;

import java.util.HashMap;
import java.util.Map;

public class FlattenMap {

    public static Map<String, Object> flattenMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        flattenMap("", map, result);
        return result;
    }

    private static void flattenMap(String prefix, Map<String, Object> map, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                // Rekursion für verschachtelte Maps
                flattenMap(key, (Map<String, Object>) value, result);
            } else {
                result.put(key, value);
            }
        }
    }
}
