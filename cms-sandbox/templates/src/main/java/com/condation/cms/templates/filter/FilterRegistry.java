package com.condation.cms.templates.filter;

import java.util.HashMap;
import java.util.Map;

public class FilterRegistry {
    private final Map<String, Filter> filters = new HashMap<>();

    public void register(String name, Filter filter) {
        filters.put(name, filter);
    }

    public Filter get(String name) {
        return filters.get(name);
    }

    public boolean exists(String name) {
        return filters.containsKey(name);
    }
}
