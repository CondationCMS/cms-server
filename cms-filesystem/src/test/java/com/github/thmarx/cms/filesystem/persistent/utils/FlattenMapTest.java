package com.github.thmarx.cms.filesystem.persistent.utils;

import com.github.thmarx.cms.filesystem.metadata.persistent.utils.FlattenMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;


public class FlattenMapTest {

    @Test
    public void testFlattenMap() {
        // Beispielinput
        Map<String, Object> nestedMap = new HashMap<>();
        Map<String, Object> nestedLevel1 = new HashMap<>();
        Map<String, Object> nestedLevel2 = new HashMap<>();
        
        nestedLevel2.put("key3", "value3");
        nestedLevel1.put("key2", nestedLevel2);
        nestedMap.put("key1", nestedLevel1);
        nestedMap.put("key4", "value4");
        
        // Erwartete flache Map
        Map<String, String> expectedFlatMap = new HashMap<>();
        expectedFlatMap.put("key1.key2.key3", "value3");
        expectedFlatMap.put("key4", "value4");
        
        // Flache Map erzeugen
        Map<String, Object> actualFlatMap = FlattenMap.flattenMap(nestedMap);
        
        // Überprüfen, ob die flache Map korrekt ist
        Assertions.assertThat(actualFlatMap).isEqualTo(expectedFlatMap);
    }

    @Test
    public void testFlattenMapWithEmptyMap() {
        // Leere Map
        Map<String, Object> emptyMap = new HashMap<>();
        
        // Erwartete flache Map
        Map<String, String> expectedFlatMap = new HashMap<>();
        
        // Flache Map erzeugen
        Map<String, Object> actualFlatMap = FlattenMap.flattenMap(emptyMap);
        
        // Überprüfen, ob die flache Map korrekt ist
        Assertions.assertThat(actualFlatMap).isEqualTo(expectedFlatMap);
    }

    @Test
    public void testFlattenMapWithSingleLevelMap() {
        // Einfache Map ohne Verschachtelung
        Map<String, Object> singleLevelMap = new HashMap<>();
        singleLevelMap.put("key1", "value1");
        singleLevelMap.put("key2", "value2");
        
        // Erwartete flache Map
        Map<String, String> expectedFlatMap = new HashMap<>();
        expectedFlatMap.put("key1", "value1");
        expectedFlatMap.put("key2", "value2");
        
        // Flache Map erzeugen
        Map<String, Object> actualFlatMap = FlattenMap.flattenMap(singleLevelMap);
        
        // Überprüfen, ob die flache Map korrekt ist
        Assertions.assertThat(actualFlatMap).isEqualTo(expectedFlatMap);
    }
}