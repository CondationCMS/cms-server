package com.condation.cms.maven.add;

/*-
 * #%L
 * CondationCMS Maven Plugin
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Map;

/**
 * Everything needed to render one extension class.
 *
 * @param packageName target package
 * @param className   simple class name
 * @param options     answers to the {@link Question}s of the type, keyed by {@link Question#key()}
 */
public record ExtensionSpec(String packageName, String className, Map<String, String> options) {

    public String option(String key) {
        var value = options.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing option '" + key + "'");
        }
        return value;
    }
}
