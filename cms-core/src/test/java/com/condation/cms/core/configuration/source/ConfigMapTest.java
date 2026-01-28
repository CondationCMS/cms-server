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
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * JUnit 5 + AssertJ Tests for ConfigMap
 * 
 * Tests lazy resolution of environment variables in configuration maps
 * with support for nested maps and recursive wrapping.
 * 
 * @author testauthor
 */
@DisplayName("ConfigMap Tests")
class ConfigMapTest {

    private ConfigMap configMap;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create temporary directory for test .env file
        tempDir = Files.createTempDirectory("config-test");
        
		System.setProperty("cms.home", tempDir.toAbsolutePath().toString());
		
        // Create .env file for tests
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile,
            "DB_HOST=localhost\n" +
            "DB_PORT=5432\n" +
            "DB_NAME=testdb\n" +
            "DB_USER=admin\n" +
            "PASSWORD=secret123\n" +
            "API_KEY=test_key_123\n" +
            "PATH_VAR=/home/user/config\n"
        );
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
        Map<String, Object> original = new HashMap<>();
        original.put("app_name", "MyApp");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("app_name");
        
        // Then
        assertThat(result)
            .isEqualTo("MyApp");
    }

    @Test
    @DisplayName("should resolve environment variable in string")
    void testGetWithEnvVar() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("database_url", "jdbc:postgresql://${env:DB_HOST}:${env:DB_PORT}/${env:DB_NAME}");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("database_url");
        
        // Then
        assertThat(result)
            .isNotNull()
            .asString()
            .doesNotContain("${env:DB_HOST}")
            .doesNotContain("${env:DB_PORT}")
            .doesNotContain("${env:DB_NAME}")
            .contains("localhost")
            .contains("5432")
            .contains("testdb");
    }

    @Test
    @DisplayName("should return null for non-existent key")
    void testGetNonExistentKey() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("non_existent_key");
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should return null value directly")
    void testGetNullValue() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("nullable_key", null);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("nullable_key");
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should return integer value unchanged")
    void testGetIntegerValue() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("port", 8080);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("port");
        
        // Then
        assertThat(result)
            .isEqualTo(8080)
            .isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("should return boolean value unchanged")
    void testGetBooleanValue() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("debug_mode", true);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("debug_mode");
        
        // Then
        assertThat(result)
            .isEqualTo(true)
            .isInstanceOf(Boolean.class);
    }

    // ==================== getOrDefault Tests ====================

    @Test
    @DisplayName("should return value when key exists")
    void testGetOrDefaultWithExistingKey() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("existing_key", "existing_value");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.getOrDefault("existing_key", "default_value");
        
        // Then
        assertThat(result)
            .isEqualTo("existing_value")
            .isNotEqualTo("default_value");
    }

    @Test
    @DisplayName("should return default value when key does not exist")
    void testGetOrDefaultWithNonExistentKey() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.getOrDefault("non_existent_key", "default_value");
        
        // Then
        assertThat(result)
            .isEqualTo("default_value");
    }

    @Test
    @DisplayName("should resolve env vars in getOrDefault value")
    void testGetOrDefaultWithEnvVar() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("api_endpoint", "https://api.example.com/?key=${env:API_KEY}");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.getOrDefault("api_endpoint", "https://default.com");
        
        // Then
        assertThat(result)
            .asString()
            .doesNotContain("${env:API_KEY}")
            .contains("test_key_123");
    }

    @Test
    @DisplayName("should return null default when key missing")
    void testGetOrDefaultWithNullDefault() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.getOrDefault("missing_key", null);
        
        // Then
        assertThat(result).isNull();
    }

    // ==================== Nested Map Tests ====================

    @Test
    @DisplayName("should wrap nested map in ConfigMap")
    void testGetNestedMapWrapping() {
        // Given
        Map<String, Object> nested = new HashMap<>();
        nested.put("host", "${env:DB_HOST}");
        nested.put("port", "${env:DB_PORT}");
        
        Map<String, Object> original = new HashMap<>();
        original.put("database", nested);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("database");
        
        // Then
        assertThat(result)
            .isInstanceOf(ConfigMap.class);
        
        ConfigMap nestedConfig = (ConfigMap) result;
        assertThat(nestedConfig.get("host"))
            .asString()
            .isEqualTo("localhost");
        assertThat(nestedConfig.get("port"))
            .asString()
            .isEqualTo("5432");
    }

    @Test
    @DisplayName("should handle deeply nested maps recursively")
    void testGetDeeplyNestedMaps() {
        // Given
        Map<String, Object> level3 = new HashMap<>();
        level3.put("user", "${env:DB_USER}");
        
        Map<String, Object> level2 = new HashMap<>();
        level2.put("auth", level3);
        
        Map<String, Object> original = new HashMap<>();
        original.put("config", level2);
        configMap = new ConfigMap(original);
        
        // When
        Object config = configMap.get("config");
        ConfigMap configMap2 = (ConfigMap) config;
        Object auth = configMap2.get("auth");
        ConfigMap configMap3 = (ConfigMap) auth;
        Object user = configMap3.get("user");
        
        // Then
        assertThat(user)
            .isEqualTo("admin");
    }

    @Test
    @DisplayName("should resolve env vars in nested maps")
    void testGetNestedMapWithEnvVars() {
        // Given
        Map<String, Object> nested = new HashMap<>();
        nested.put("connection_string", "Server=${env:DB_HOST};Database=${env:DB_NAME};User=${env:DB_USER}");
        
        Map<String, Object> original = new HashMap<>();
        original.put("database_config", nested);
        configMap = new ConfigMap(original);
        
        // When
        ConfigMap nestedConfig = (ConfigMap) configMap.get("database_config");
        Object result = nestedConfig.get("connection_string");
        
        // Then
        assertThat(result)
            .asString()
            .doesNotContain("${env:")
            .contains("Server=localhost")
            .contains("Database=testdb")
            .contains("User=admin");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("should handle multiple env vars in single string")
    void testMultipleEnvVarsInString() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("jdbc_url", 
            "jdbc:postgresql://${env:DB_HOST}:${env:DB_PORT}/${env:DB_NAME}?user=${env:DB_USER}&password=${env:PASSWORD}");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("jdbc_url");
        
        // Then
        assertThat(result)
            .asString()
            .doesNotContain("${env:")
            .contains("localhost")
            .contains("5432")
            .contains("testdb")
            .contains("admin")
            .contains("secret123");
    }

    @Test
    @DisplayName("should keep unresolved env vars as placeholder")
    void testUnresolvedEnvVar() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("missing_var", "${env:NON_EXISTENT_VAR}");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("missing_var");
        
        // Then
        assertThat(result)
            .asString()
            .isEqualTo("${env:NON_EXISTENT_VAR}");
    }

    @Test
    @DisplayName("should handle empty string")
    void testEmptyString() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("empty", "");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("empty");
        
        // Then
        assertThat(result)
            .isEqualTo("")
            .isInstanceOf(String.class);
    }

    @Test
    @DisplayName("should handle string with no env vars")
    void testStringWithoutEnvVars() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("plain", "This is a plain string without variables");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("plain");
        
        // Then
        assertThat(result)
            .isEqualTo("This is a plain string without variables");
    }

    @Test
    @DisplayName("should handle mixed content with env vars")
    void testMixedContent() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("message", "Welcome ${env:DB_USER}, your database is ${env:DB_HOST}:${env:DB_PORT}");
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("message");
        
        // Then
        assertThat(result)
            .asString()
            .isEqualTo("Welcome admin, your database is localhost:5432");
    }

    // ==================== Data Type Tests ====================

    @ParameterizedTest
    @DisplayName("should preserve non-string data types")
    @ValueSource(ints = {0, 1, 42, -1, 99999})
    void testIntegerValues(int value) {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("number", value);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("number");
        
        // Then
        assertThat(result)
            .isEqualTo(value)
            .isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("should handle double values")
    void testDoubleValues() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("ratio", 3.14);
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("ratio");
        
        // Then
        assertThat(result)
            .isEqualTo(3.14)
            .isInstanceOf(Double.class);
    }

    @Test
    @DisplayName("should handle list values")
    void testListValues() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("servers", java.util.List.of("server1", "server2", "server3"));
        configMap = new ConfigMap(original);
        
        // When
        Object result = configMap.get("servers");
        
        // Then
        assertThat(result)
            .isInstanceOf(java.util.List.class)
            .isEqualTo(java.util.List.of("server1", "server2", "server3"));
    }

    // ==================== Consistency Tests ====================

    @Test
    @DisplayName("should return same result on multiple get calls")
    void testConsistentResults() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("config_key", "prefix_${env:DB_HOST}_suffix");
        configMap = new ConfigMap(original);
        
        // When
        Object result1 = configMap.get("config_key");
        Object result2 = configMap.get("config_key");
        Object result3 = configMap.get("config_key");
        
        // Then
        assertThat(result1)
            .isEqualTo(result2)
            .isEqualTo(result3)
            .asString()
            .isEqualTo("prefix_localhost_suffix");
    }

    @Test
    @DisplayName("should work with getOrDefault and get interchangeably")
    void testGetAndGetOrDefaultConsistency() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key", "value_${env:DB_HOST}");
        configMap = new ConfigMap(original);
        
        // When
        Object result1 = configMap.get("key");
        Object result2 = configMap.getOrDefault("key", "default");
        
        // Then
        assertThat(result1)
            .isEqualTo(result2);
    }

    @Test
    @DisplayName("should map is initialized with all original values")
    void testMapInitialization() {
        // Given
        Map<String, Object> original = new HashMap<>();
        original.put("key1", "value1");
        original.put("key2", "value2");
        original.put("key3", "${env:DB_HOST}");
        
        // When
        configMap = new ConfigMap(original);
        
        // Then
        assertThat(configMap)
            .hasSize(3)
            .containsKeys("key1", "key2", "key3");
    }
}
