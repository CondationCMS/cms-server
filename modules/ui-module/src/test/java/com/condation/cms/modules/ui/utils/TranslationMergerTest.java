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

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

public class TranslationMergerTest {

    @Test
    void shouldMergeSourceIntoTargetCorrectly() {
        // given
        Map<String, Map<String, String>> target = new HashMap<>();
        target.put("en", new HashMap<>(Map.of("hello", "Hello")));
        target.put("de", new HashMap<>(Map.of("hello", "Hallo")));

        Map<String, Map<String, String>> source = Map.of(
            "en", Map.of("bye", "Goodbye"),
            "de", Map.of("thanks", "Danke"),
            "fr", Map.of("hello", "Bonjour")
        );

        // when
        TranslationMerger.mergeTranslationMaps(source, target);

        // then
        assertThat(target).containsOnlyKeys("en", "de", "fr");

        assertThat(target.get("en"))
            .containsEntry("hello", "Hello")
            .containsEntry("bye", "Goodbye");

        assertThat(target.get("de"))
            .containsEntry("hello", "Hallo")
            .containsEntry("thanks", "Danke");

        assertThat(target.get("fr"))
            .containsEntry("hello", "Bonjour");
    }

    @Test
    void shouldOverrideExistingValuesInTarget() {
        // given
        Map<String, Map<String, String>> target = new HashMap<>();
        target.put("en", new HashMap<>(Map.of("hello", "Hello")));

        Map<String, Map<String, String>> source = Map.of(
            "en", Map.of("hello", "Hi")
        );

        // when
        TranslationMerger.mergeTranslationMaps(source, target);

        // then
        assertThat(target.get("en"))
            .containsEntry("hello", "Hi"); // source value overrides existing one
    }

    @Test
    void shouldNotFailWhenTargetIsInitiallyEmpty() {
        // given
        Map<String, Map<String, String>> target = new HashMap<>();
        Map<String, Map<String, String>> source = Map.of(
            "en", Map.of("hello", "Hello")
        );

        // when
        TranslationMerger.mergeTranslationMaps(source, target);

        // then
        assertThat(target)
            .containsOnlyKeys("en")
            .containsEntry("en", Map.of("hello", "Hello"));
    }
}
