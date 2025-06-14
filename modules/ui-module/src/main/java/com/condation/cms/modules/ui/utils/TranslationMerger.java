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

import java.util.*;

public class TranslationMerger {

    /**
     * Merges the entries of the given source map into the target map.
     * The target map will be modified directly.
     *
     * @param source The map containing new values to merge
     * @param target The map into which values will be merged
     */
    public static void mergeTranslationMaps(Map<String, Map<String, String>> source,
                                            Map<String, Map<String, String>> target) {
        for (Map.Entry<String, Map<String, String>> languageEntry : source.entrySet()) {
            String language = languageEntry.getKey();
            Map<String, String> newTranslations = languageEntry.getValue();

            // Ensure the inner map exists in the target
            target.computeIfAbsent(language, k -> new HashMap<>())
                  .putAll(newTranslations); // Overwrites existing keys if necessary
        }
    }
}
