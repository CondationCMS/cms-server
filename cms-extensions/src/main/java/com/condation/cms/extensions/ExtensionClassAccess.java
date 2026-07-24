package com.condation.cms.extensions;

/*-
 * #%L
 * CMS Extensions
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

import java.util.List;
import java.util.Set;

public final class ExtensionClassAccess {

    private static final List<String> ALLOWED_PACKAGES = List.of(
        "com.condation.cms.api.",
        "java.time.",
        "java.util.",
		"java.nio.charset.StandardCharsets"
    );

    private static final Set<String> BLOCKED_CLASSES = Set.of(
        "java.lang.Runtime",
        "java.lang.ProcessBuilder",
        "java.lang.System",
        "java.lang.Class",
        "java.lang.ClassLoader"
    );

    private ExtensionClassAccess() {
    }

    public static boolean isAllowed(String className) {
        if (BLOCKED_CLASSES.contains(className)) {
            return false;
        }

        return ALLOWED_PACKAGES.stream()
            .anyMatch(className::startsWith);
    }
}
