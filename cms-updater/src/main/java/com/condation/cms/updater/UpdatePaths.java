package com.condation.cms.updater;

/*-
 * #%L
 * CondationCMS Updater
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

import java.nio.file.Path;

final class UpdatePaths {
    private UpdatePaths() { }

    static Path resolveInside(Path root, String relative) {
        if (relative == null || relative.isBlank()) {
            throw new IllegalArgumentException("Update path must not be empty");
        }
        Path supplied = Path.of(relative);
        if (supplied.isAbsolute()) {
            throw new IllegalArgumentException("Absolute update paths are not allowed: " + relative);
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(supplied).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("Update path escapes installation directory: " + relative);
        }
        return resolved;
    }
}
