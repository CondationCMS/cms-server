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
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipFile;

/**
 * Installs a module zip (<code>&lt;id&gt;/module.properties</code>,
 * <code>&lt;id&gt;/libs/*.jar</code>) into <code>&lt;home&gt;/modules/&lt;id&gt;/</code>.
 */
public final class ModuleInstaller {

    private ModuleInstaller() {
    }

    public static String install(Path zip, Path cmsHome) throws IOException {
        if (!Files.isRegularFile(zip)) {
            throw new IOException("Module zip not found: " + zip + " - run `mvn package` first");
        }
        var modulesDir = cmsHome.resolve("modules").toAbsolutePath().normalize();

        try (var zipFile = new ZipFile(zip.toFile())) {
            var id = topLevelFolder(zipFile);
            var target = modulesDir.resolve(id).normalize();
            if (!target.startsWith(modulesDir) || target.equals(modulesDir)) {
                throw new IOException("Illegal module id in zip: " + id);
            }

            // validate all entries before touching the file system
            var entries = zipFile.stream().toList();
            for (var entry : entries) {
                resolveEntryPath(target, id, entry.getName(), entry.isDirectory());
            }

            deleteRecursively(target);
            Files.createDirectories(target);
            for (var entry : entries) {
                var path = resolveEntryPath(target, id, entry.getName(), entry.isDirectory());
                if (entry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    Files.createDirectories(path.getParent());
                    try (var in = zipFile.getInputStream(entry)) {
                        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            return id;
        }
    }

    private static Path resolveEntryPath(Path target, String id, String entryName, boolean directory) throws IOException {
        var name = entryName.replace('\\', '/');
        if (name.startsWith("/") || (name.length() >= 2 && Character.isLetter(name.charAt(0)) && name.charAt(1) == ':')) {
            throw new IOException("Illegal absolute zip entry: " + entryName);
        }

        var segments = name.split("/", -1);
        if (segments.length == 0 || !segments[0].equals(id)) {
            throw new IOException("Illegal zip entry (outside of module folder): " + entryName);
        }
        for (int i = 0; i < segments.length; i++) {
            var segment = segments[i];
            var trailingDirectorySeparator = directory && i == segments.length - 1 && segment.isEmpty();
            if (segment.equals(".") || segment.equals("..")
                    || (segment.isEmpty() && !trailingDirectorySeparator && !(i == 0 && segments.length == 1))) {
                throw new IOException("Illegal zip entry path: " + entryName);
            }
        }

        var relativeName = name.substring(id.length());
        if (relativeName.startsWith("/")) {
            relativeName = relativeName.substring(1);
        }
        var path = relativeName.isEmpty() ? target : target.resolve(relativeName).normalize();
        if (!path.startsWith(target)) {
            throw new IOException("Illegal zip entry (outside of module folder): " + entryName);
        }
        return path;
    }

    private static String topLevelFolder(ZipFile zipFile) throws IOException {
        var folders = zipFile.stream()
                .map(e -> e.getName().replace('\\', '/'))
                .map(name -> name.contains("/") ? name.substring(0, name.indexOf('/')) : name)
                .distinct()
                .toList();
        if (folders.size() != 1 || folders.get(0).isBlank() || folders.get(0).equals(".") || folders.get(0).equals("..")) {
            throw new IOException("Module zip must contain exactly one top-level folder, found: " + folders);
        }
        return folders.get(0);
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            for (var path : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }
}
