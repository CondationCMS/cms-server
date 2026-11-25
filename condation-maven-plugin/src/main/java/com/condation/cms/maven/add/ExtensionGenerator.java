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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Renders an extension and writes it into the source directory.
 */
public final class ExtensionGenerator {

    private ExtensionGenerator() {
    }

    /**
     * @return the file the extension class is written to
     */
    public static Path target(Path sourceDirectory, ExtensionSpec spec) {
        return sourceDirectory
                .resolve(spec.packageName().replace('.', '/'))
                .resolve(spec.className() + ".java");
    }

    /**
     * @throws IllegalArgumentException  if package, class name or an option is invalid; nothing is written then
     * @throws java.nio.file.FileAlreadyExistsException if the file exists and <code>overwrite</code> is false
     */
    public static Path write(Path sourceDirectory, ExtensionType type, ExtensionSpec spec, boolean overwrite)
            throws IOException {
        Names.requirePackage(spec.packageName());
        Names.requireClassName(spec.className());
        // render first: invalid options must not leave anything behind
        var source = type.render(spec);

        var file = target(sourceDirectory, spec);
        Files.createDirectories(file.getParent());
        if (overwrite) {
            Files.writeString(file, source, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } else {
            Files.writeString(file, source, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        }
        return file;
    }
}
