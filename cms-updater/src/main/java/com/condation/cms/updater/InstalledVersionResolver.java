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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

/** Finds the current CMS server JAR in the installation root and reads its embedded version. */
public final class InstalledVersionResolver {
    private static final String SERVER_JAR_PREFIX = "cms-server";
    private static final String VERSION_RESOURCE = "com/condation/cms/application.properties";

    public String resolve(Path installationDirectory) throws IOException {
        Path home = installationDirectory.toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();
        try (var entries = Files.list(home)) {
            entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(SERVER_JAR_PREFIX)
                            && path.getFileName().toString().endsWith(".jar"))
                    .forEach(candidates::add);
        }
        if (candidates.isEmpty()) throw new IOException("Could not find a cms-server JAR in installation home: " + home);
        if (candidates.size() > 1) throw new IOException("Found multiple cms-server JARs in installation home: " + candidates);

        Path serverJar = candidates.get(0);
        String embeddedVersion = readEmbeddedVersion(serverJar);
        if (embeddedVersion != null && embeddedVersion.matches("\\d+(?:\\.\\d+){1,2}")) return embeddedVersion;

        String filename = serverJar.getFileName().toString();
        var matcher = java.util.regex.Pattern.compile("^cms-server-(\\d+(?:\\.\\d+){1,2})\\.jar$").matcher(filename);
        if (matcher.matches()) return matcher.group(1);
        throw new IOException("Could not determine CMS version from " + serverJar);
    }

    private String readEmbeddedVersion(Path serverJar) throws IOException {
        try (JarFile jar = new JarFile(serverJar.toFile())) {
            var entry = jar.getJarEntry(VERSION_RESOURCE);
            if (entry == null) return null;
            Properties properties = new Properties();
            try (var input = jar.getInputStream(entry)) { properties.load(input); }
            return properties.getProperty("version");
        }
    }
}
