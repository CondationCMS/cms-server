package com.condation.cms.core.configuration;

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

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * JUnit 5 + AssertJ Tests for EnvironmentVariables.resolveEnvVars()
 * 
 * IMPORTANT: Tests are configured to properly set environment variables
 * @author testauthor
 */
@DisplayName("EnvironmentVariables Tests")
class EnvironmentVariablesTest {

    private EnvironmentVariables environmentVariables;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create temporary directory for test .env file
        tempDir = Files.createTempDirectory("env-test");
        
        // Create simple .env file for tests
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile,
            "TEST_VAR=Hello World\n" +
            "DB_HOST=localhost\n" +
            "DB_PORT=5432\n" +
            "PATH_WITH_SPECIAL=$pecial/\\\\Path\n" +
            "EMPTY_VAR=\n" +
            "VAR_123=value123\n" +
            "_UNDERSCORE_VAR=underscore_value\n"
        );
        
        environmentVariables = new EnvironmentVariables(tempDir);
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

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("should replace simple environment variable")
    void testResolveSimpleEnvVar() {
        // Given
        String input = "Value: ${env:TEST_VAR}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isNotNull()
            .contains("Value:")
            .contains("Hello World")
            .doesNotContain("${env:TEST_VAR}");
    }

    @Test
    @DisplayName("should replace multiple environment variables")
    void testResolveMultipleEnvVars() {
        // Given
        String input = "Host: ${env:DB_HOST}, Port: ${env:DB_PORT}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .doesNotContain("${env:DB_HOST}")
            .doesNotContain("${env:DB_PORT}")
            .contains("Host:")
            .contains("localhost")
            .contains("5432");
    }

    @Test
    @DisplayName("should process environment variables with underscores")
    void testResolveEnvVarWithUnderscores() {
        // Given
        String input = "Variable: ${env:TEST_VAR}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isNotEmpty()
            .doesNotContain("${env:TEST_VAR}")
            .contains("Hello World");
    }

    @Test
    @DisplayName("should safely handle text with regex special characters")
    void testResolveEnvVarWithSpecialChars() {
        // Given - environment variable with regex special characters
        String input = "Path: ${env:PATH_WITH_SPECIAL}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .doesNotContain("${env:PATH_WITH_SPECIAL}")
            .contains("Path:");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("should safely handle null input")
    void testResolveNullInput() {
        // When
        String result = environmentVariables.resolveEnvVars(null);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should return empty string")
    void testResolveEmptyString() {
        // When
        String result = environmentVariables.resolveEnvVars("");
        
        // Then
        assertThat(result)
            .isNotNull()
            .isEmpty();
    }

    @Test
    @DisplayName("should return plain text string unchanged")
    void testResolveStringWithoutEnvVars() {
        // Given
        String input = "This is a plain text without variables";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isEqualTo(input)
            .isEqualTo("This is a plain text without variables");
    }

    @Test
    @DisplayName("should keep non-existent variable as placeholder")
    void testResolveNonExistentEnvVar() {
        // Given
        String input = "Value: ${env:NON_EXISTENT_VAR}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isNotNull()
            .contains("${env:NON_EXISTENT_VAR}")
            .startsWith("Value:");
    }

    // ==================== Format & Pattern Tests ====================

    @Test
    @DisplayName("should replace only correct ${env:NAME} syntax")
    void testResolveOnlyValidFormat() {
        // Given - different formats, only one should be replaced
        String input = "${env:TEST_VAR} and $env:TEST_VAR and ${TEST_VAR}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .doesNotContain("${env:TEST_VAR}")
            .contains("$env:TEST_VAR")  // not replaced
            .contains("${TEST_VAR}");    // not replaced
    }

    @Test
    @DisplayName("should replace variables at beginning, middle, and end")
    void testResolveVarAtDifferentPositions() {
        // Given
        String input = "${env:DB_HOST}:${env:DB_PORT}/database/${env:DB_HOST}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .doesNotContain("${env:")
            .startsWith("localhost")
            .contains(":5432/");
    }

    // ==================== Performance & Robustness ====================

    @Test
    @DisplayName("should handle long strings with many variables")
    void testResolveLongStringWithManyVars() {
        // Given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("${env:DB_HOST}:");
        }
        String input = sb.toString();
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .doesNotContain("${env:DB_HOST}")
            .contains("localhost")
            .hasSizeGreaterThanOrEqualTo(100); // at least 100 characters
    }

    @Test
    @DisplayName("should process variables with numbers")
    void testResolveEnvVarWithNumbers() {
        // Given - VAR_123 is defined in the .env file
        String input = "${env:VAR_123}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isNotEmpty()
            .doesNotContain("${env:")
            .contains("value123");
    }

    @ParameterizedTest
    @DisplayName("should process different variable names")
    @ValueSource(strings = {
        "${env:DB_HOST}",
        "${env:TEST_VAR}",
        "${env:VAR_123}",
        "${env:_UNDERSCORE_VAR}"
    })
    void testResolveVariousVarNames(String input) {
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isNotNull()
            .doesNotStartWith("${env:")  // variable should be replaced
            .isNotEqualTo(input);         // result should differ from input
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("should process complex configuration strings")
    void testResolveComplexConfigString() {
        // Given
        String input = "jdbc:postgresql://${env:DB_HOST}:${env:DB_PORT}/mydb";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .startsWith("jdbc:postgresql://")
            .doesNotContain("${env:DB_HOST}")
            .doesNotContain("${env:DB_PORT}")
            .contains("localhost")
            .contains("5432");
    }

    @Test
    @DisplayName("should process JSON with environment variables")
    void testResolveJsonWithEnvVars() {
        // Given
        String input = "{\"host\":\"${env:DB_HOST}\",\"port\":${env:DB_PORT}}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .startsWith("{\"host\":")
            .endsWith("}")
            .doesNotContain("${env:")
            .contains("localhost")
            .contains("5432");
    }

    @Test
    @DisplayName("should be callable multiple times consecutively")
    void testResolveCalledMultipleTimes() {
        // Given
        String input = "Value: ${env:TEST_VAR}";
        
        // When
        String result1 = environmentVariables.resolveEnvVars(input);
        String result2 = environmentVariables.resolveEnvVars(input);
        String result3 = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result1)
            .isEqualTo(result2)
            .isEqualTo(result3)
            .contains("Hello World");
    }

    @Test
    @DisplayName("should return plain text as-is when no variables present")
    void testResolvePlainTextPreserved() {
        // Given
        String input = "This is plain text with no template syntax at all";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .isEqualTo(input)
            .isNotNull()
            .isNotEmpty();
    }

    @Test
    @DisplayName("should handle mixed plain text and variables")
    void testResolveMixedTextAndVars() {
        // Given - combination of plain text and variables
        String input = "Configuration: Host=" + "${env:DB_HOST}" + " Port=" + "${env:DB_PORT}";
        
        // When
        String result = environmentVariables.resolveEnvVars(input);
        
        // Then
        assertThat(result)
            .contains("Configuration:")
            .contains("Host=")
            .contains("Port=")
            .doesNotContain("${env:")
            .contains("localhost")
            .contains("5432");
    }
}
