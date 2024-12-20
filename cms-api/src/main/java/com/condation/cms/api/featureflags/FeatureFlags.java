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
     * @param featureName Der Name des Features.
     * @return true, wenn das Feature aktiviert ist; andernfalls false.
     */
    public static boolean isEnabled(String featureName) {
        return flags.getOrDefault(featureName, false);
    }

    /**
     * Setzt oder aktualisiert ein einzelnes Feature-Flag.
     * @param featureName Der Name des Features.
     * @param enabled true für aktivieren, false für deaktivieren.
     */
    public static void setFlag(String featureName, boolean enabled) {
        flags.put(featureName, enabled);
    }

    /**
     * Gibt eine unveränderliche Sicht auf die Flags zurück.
     * @return Map der aktuellen Feature-Flags.
     */
    public static Map<String, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }
}
