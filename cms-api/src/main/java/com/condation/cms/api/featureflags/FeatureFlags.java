package com.condation.cms.api.featureflags;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureFlags {
    private static final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    private FeatureFlags() {}

    /**
     * Initialize Feature-Flags.
     * @param initialFlags Map of features
     */
    public static void initialize(Map<String, Boolean> initialFlags) {
        flags.clear();
        flags.putAll(initialFlags);
    }

    /**
     * Checks if a feature is activated
     * @param featureName Name of the feature.
     * @return true, if the feature is activated; otherwise false.
     */
    public static boolean isEnabled(String featureName) {
        return flags.getOrDefault(featureName, false);
    }

    /**
     * sets or updates a feature
     * @param featureName Name of the feature.
     * @param enabled true if activated, false if deactivated.
     */
    public static void setFlag(String featureName, boolean enabled) {
        flags.put(featureName, enabled);
    }

    /**
     * returns an unmodifiable view of the feature flags
     * @return Map of the current feature flags.
     */
    public static Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }
}
