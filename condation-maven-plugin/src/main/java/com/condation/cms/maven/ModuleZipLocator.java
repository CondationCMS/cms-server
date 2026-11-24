package com.condation.cms.maven;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Finds the module zip: explicit configuration, then the zip attached by the assembly
 * plugin (classifier <code>bin</code>), then the single <code>*-bin.zip</code> in the build directory.
 */
public final class ModuleZipLocator {

    private ModuleZipLocator() {
    }

    public static Path locate(Path explicit, Path attached, Path targetDir) throws IOException {
        if (explicit != null) {
            return explicit;
        }
        if (attached != null && Files.isRegularFile(attached)) {
            return attached;
        }
        List<Path> candidates;
        if (Files.isDirectory(targetDir)) {
            try (var stream = Files.list(targetDir)) {
                candidates = stream.filter(p -> p.getFileName().toString().endsWith("-bin.zip")).sorted().toList();
            }
        } else {
            candidates = List.of();
        }
        if (candidates.isEmpty()) {
            throw new IOException("No module zip (*-bin.zip) found in " + targetDir
                    + " - run `mvn package` first or set condation.moduleZip");
        }
        if (candidates.size() > 1) {
            throw new IOException("Several module zips found in " + targetDir + " " + candidates
                    + " - set condation.moduleZip");
        }
        return candidates.get(0);
    }
}
