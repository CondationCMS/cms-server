package com.condation.cms.core.configuration.source;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * JUnit 5 + AssertJ Tests for ConfigList
 * 
 * Tests lazy resolution of environment variables in lists
 * with support for nested structures and recursive wrapping.
 * 
 * @author testauthor
 */
@DisplayName("ConfigList Tests")
class ConfigListTest {

    private ConfigList configList;
	@TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create .env file for tests
        Path envFile = tempDir.resolve(".env");
		System.setProperty("cms.home", tempDir.toAbsolutePath().toString());
		
        Files.writeString(envFile, """
								   SERVER_1=server1.example.com
								   SERVER_2=server2.example.com
								   SERVER_3=server3.example.com
								   PORT=8080
								   API_URL=https://api.example.com
								   DB_HOST=localhost
								   DB_PORT=5432
								   """);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up temporary directory
        Files.walk(tempDir)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    // Ignore
                }
            });
    }

    // ==================== Basic Get Tests ====================

    @Test
    @DisplayName("should return simple string value unchanged")
    void testGetSimpleStringValue() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("value1");
        original.add("value2");
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .isEqualTo("value1");
    }

    @Test
    @DisplayName("should resolve environment variable in string")
    void testGetWithEnvVar() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("Server: ${env:SERVER_1}");
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .asString()
            .doesNotContain("${env:SERVER_1}")
            .contains("server1.example.com");
    }

    @Test
    @DisplayName("should handle multiple elements with env vars")
    void testGetMultipleElementsWithEnvVars() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When & Then
        assertThat(configList.get(0))
            .asString()
            .isEqualTo("server1.example.com");
        assertThat(configList.get(1))
            .asString()
            .isEqualTo("server2.example.com");
        assertThat(configList.get(2))
            .asString()
            .isEqualTo("server3.example.com");
    }

    @Test
    @DisplayName("should return null value directly")
    void testGetNullValue() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("value");
        original.add(null);
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(1);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should return integer value unchanged")
    void testGetIntegerValue() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add(8080);
        original.add(5432);
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .isEqualTo(8080)
            .isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("should return boolean value unchanged")
    void testGetBooleanValue() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add(true);
        original.add(false);
        configList = new ConfigList(original);
        
        // When
        Object result1 = configList.get(0);
        Object result2 = configList.get(1);
        
        // Then
        assertThat(result1).isEqualTo(true);
        assertThat(result2).isEqualTo(false);
    }

    // ==================== Nested Map Tests ====================

    @Test
    @DisplayName("should wrap nested map in ConfigMap")
    void testGetNestedMapWrapping() {
        // Given
        Map<Object, Object> nestedMap = new HashMap<>();
        nestedMap.put("server", "${env:SERVER_1}");
        nestedMap.put("port", "${env:PORT}");
        
        List<Object> original = new ArrayList<>();
        original.add(nestedMap);
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .isInstanceOf(ConfigMap.class);
        
        ConfigMap configMap = (ConfigMap) result;
        assertThat(configMap.get("server"))
            .asString()
            .isEqualTo("server1.example.com");
        assertThat(configMap.get("port"))
            .asString()
            .isEqualTo("8080");
    }

    @Test
    @DisplayName("should wrap nested list in ConfigList")
    void testGetNestedListWrapping() {
        // Given
        List<Object> nestedList = new ArrayList<>();
        nestedList.add("${env:SERVER_1}");
        nestedList.add("${env:SERVER_2}");
        
        List<Object> original = new ArrayList<>();
        original.add(nestedList);
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .isInstanceOf(ConfigList.class);
        
        ConfigList nestedConfigList = (ConfigList) result;
        assertThat(nestedConfigList.get(0))
            .asString()
            .isEqualTo("server1.example.com");
        assertThat(nestedConfigList.get(1))
            .asString()
            .isEqualTo("server2.example.com");
    }

    // ==================== Iterator Tests ====================

    @Test
    @DisplayName("should iterate with resolved values")
    void testIteratorWithResolvedValues() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When & Then
        List<Object> resolved = new ArrayList<>();
        configList.iterator().forEachRemaining(resolved::add);
        
        assertThat(resolved)
            .hasSize(3)
            .contains("server1.example.com", "server2.example.com", "server3.example.com");
    }

    @Test
    @DisplayName("should support ListIterator navigation")
    void testListIteratorNavigation() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When
        var iterator = configList.listIterator();
        String first = (String) iterator.next();
        String second = (String) iterator.next();
        String previous = (String) iterator.previous();
        
        // Then
        assertThat(first).isEqualTo("server1.example.com");
        assertThat(second).isEqualTo("server2.example.com");
        assertThat(previous).isEqualTo("server2.example.com");
    }

    @Test
    @DisplayName("should support ListIterator from index")
    void testListIteratorFromIndex() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When
        var iterator = configList.listIterator(1);
        String second = (String) iterator.next();
        
        // Then
        assertThat(second).isEqualTo("server2.example.com");
    }

    // ==================== SubList Tests ====================

    @Test
    @DisplayName("should return ConfigList sublist with resolved values")
    void testSubList() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When
        List<Object> sublist = configList.subList(1, 3);
        
        // Then
        assertThat(sublist)
            .isInstanceOf(ConfigList.class)
            .hasSize(2);
        
        ConfigList configSublist = (ConfigList) sublist;
        assertThat(configSublist.get(0))
            .asString()
            .isEqualTo("server2.example.com");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("should handle empty list")
    void testEmptyList() {
        // Given
        List<Object> original = new ArrayList<>();
        configList = new ConfigList(original);
        
        // When & Then
		assertThat(configList)
			.hasSize(0)
            .isEmpty();
    }

    @Test
    @DisplayName("should handle list with only nulls")
    void testListWithOnlyNulls() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add(null);
        original.add(null);
        configList = new ConfigList(original);
        
        // When & Then
        assertThat(configList.get(0)).isNull();
        assertThat(configList.get(1)).isNull();
    }

    @Test
    @DisplayName("should handle mixed data types")
    void testMixedDataTypes() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add(8080);
        original.add(true);
        original.add(3.14);
        configList = new ConfigList(original);
        
        // When & Then
        assertThat(configList.get(0))
            .asString()
            .isEqualTo("server1.example.com");
        assertThat(configList.get(1)).isEqualTo(8080);
        assertThat(configList.get(2)).isEqualTo(true);
        assertThat(configList.get(3)).isEqualTo(3.14);
    }

    @Test
    @DisplayName("should handle string with multiple env vars")
    void testStringWithMultipleEnvVars() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("Servers: ${env:SERVER_1}, ${env:SERVER_2}, ${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .asString()
            .doesNotContain("${env:")
            .contains("server1.example.com")
            .contains("server2.example.com")
            .contains("server3.example.com");
    }

    @Test
    @DisplayName("should handle unresolved env vars")
    void testUnresolvedEnvVars() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:MISSING_VAR}");
        configList = new ConfigList(original);
        
        // When
        Object result = configList.get(0);
        
        // Then
        assertThat(result)
            .asString()
            .isEqualTo("${env:MISSING_VAR}");
    }

    @Test
    @DisplayName("should return correct toString representation")
    void testToStringWithResolvedValues() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        configList = new ConfigList(original);
        
        // When
        String toString = configList.toString();
        
        // Then
        assertThat(toString)
            .doesNotContain("${env:")
            .contains("server1.example.com")
            .contains("server2.example.com");
    }

    // ==================== Consistency Tests ====================

    @Test
    @DisplayName("should return same result on multiple get calls")
    void testConsistentResults() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        configList = new ConfigList(original);
        
        // When
        Object result1 = configList.get(0);
        Object result2 = configList.get(0);
        Object result3 = configList.get(0);
        
        // Then
        assertThat(result1)
            .isEqualTo(result2)
            .isEqualTo(result3);
    }

    @Test
    @DisplayName("should maintain list size after get operations")
    void testListSizeConsistency() {
        // Given
        List<Object> original = new ArrayList<>();
        original.add("${env:SERVER_1}");
        original.add("${env:SERVER_2}");
        original.add("${env:SERVER_3}");
        configList = new ConfigList(original);
        
        // When
        int size1 = configList.size();
        configList.get(0);
        configList.get(1);
        configList.get(2);
        int size2 = configList.size();
        
        // Then
        assertThat(size1).isEqualTo(size2).isEqualTo(3);
    }

    @Test
    @DisplayName("should handle deeply nested structures")
    void testDeeplyNestedStructures() {
        // Given
        List<Object> level3List = new ArrayList<>();
        level3List.add("${env:SERVER_1}");
        
        Map<Object, Object> level2Map = new HashMap<>();
        level2Map.put("servers", level3List);
        
        List<Object> original = new ArrayList<>();
        original.add(level2Map);
        configList = new ConfigList(original);
        
        // When
        ConfigMap level2 = (ConfigMap) configList.get(0);
        ConfigList level3 = (ConfigList) level2.get("servers");
        String server = (String) level3.get(0);
        
        // Then
        assertThat(server).isEqualTo("server1.example.com");
    }
}
