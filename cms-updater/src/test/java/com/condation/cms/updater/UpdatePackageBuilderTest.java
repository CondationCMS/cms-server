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

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdatePackageBuilderTest {
    @TempDir Path temporary;

    @Test
    void buildsUpdateZipWithEveryLibraryAndGeneratedManifest() throws Exception {
        Path home = Files.createDirectory(temporary.resolve("release"));
        Files.createDirectory(home.resolve("lib"));
        Files.writeString(home.resolve("cms-server-8.5.0.jar"), "server");
        Files.writeString(home.resolve("lib/one.jar"), "one");
        Files.writeString(home.resolve("lib/two.jar"), "two");
        Path scripts = Files.createDirectory(temporary.resolve("scripts"));
        Files.writeString(scripts.resolve("server.sh"), "#!/bin/sh\n");
        Path output = temporary.resolve("release/condationcms-update-8.5.0.zip");

        new UpdatePackageBuilder().build(home, scripts, output, "8.5.0", "8.2.0");

        try (ZipFile zip = new ZipFile(output.toFile())) {
            var manifest = new Gson().fromJson(new String(zip.getInputStream(zip.getEntry("manifest.json")).readAllBytes()), UpdateManifest.class);
            assertEquals("8.5.0", manifest.version());
            assertEquals("8.2.0", manifest.minimumVersion());
            assertEquals(java.util.List.of("lib"), manifest.replaceDirectories());
            assertEquals(java.util.List.of("cms-server-*.jar"), manifest.removePatterns());
            assertEquals(4, manifest.files().size());
            assertNotNull(zip.getEntry("files/lib/one.jar"));
            assertNotNull(zip.getEntry("files/lib/two.jar"));
            assertNotNull(zip.getEntry("files/server.sh"));
            assertTrue(manifest.files().stream().allMatch(file -> file.sha256().matches("[0-9a-f]{64}")));
        }
    }
}
