package com.condation.cms.content;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;


public class Namespace {

    @Getter
    Map<String, Map<String, Object>> namespaces = new HashMap<>();
    
    public void add (String namespace, String key, Object object) {
        var namespaceMap = namespaces.computeIfAbsent(namespace, k -> new HashMap<>());
        namespaceMap.put(key, object);
    }

}
