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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModuleInstallerTest {

    @TempDir
    Path tmp;

    private Path zip(String name, Map<String, String> entries) throws IOException {
        var zip = tmp.resolve(name);
        try (OutputStream out = Files.newOutputStream(zip); var zos = new ZipOutputStream(out)) {
            for (var e : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(e.getKey()));
                zos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return zip;
    }

    private Path home() throws IOException {
        return Files.createDirectories(tmp.resolve("home"));
    }

    @Test
    void should_extract_module_into_home_modules_when_zip_valid() throws IOException {
        var entries = new LinkedHashMap<String, String>();
        entries.put("m1/module.properties", "id=m1");
        entries.put("m1/libs/a.jar", "jar");
        var home = home();

        var id = ModuleInstaller.install(zip("m.zip", entries), home);

        assertThat(id).isEqualTo("m1");
        assertThat(home.resolve("modules/m1/module.properties")).hasContent("id=m1");
        assertThat(home.resolve("modules/m1/libs/a.jar")).hasContent("jar");
    }

    @Test
    void should_replace_existing_module_folder_when_installed_again() throws IOException {
        var home = home();
        Files.createDirectories(home.resolve("modules/m1"));
        Files.writeString(home.resolve("modules/m1/old.txt"), "old");

        ModuleInstaller.install(zip("m.zip", Map.of("m1/module.properties", "id=m1")), home);

        assertThat(home.resolve("modules/m1/old.txt")).doesNotExist();
        assertThat(home.resolve("modules/m1/module.properties")).exists();
    }

    @Test
    void should_fail_with_hint_when_zip_missing() throws IOException {
        var home = home();
        assertThatThrownBy(() -> ModuleInstaller.install(tmp.resolve("missing.zip"), home))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("mvn package");
    }

    @Test
    void should_reject_entry_escaping_module_folder_when_zip_slip() throws IOException {
        var home = home();
        var zip = zip("evil.zip", Map.of("m1/../../evil.txt", "x"));

        assertThatThrownBy(() -> ModuleInstaller.install(zip, home))
                .isInstanceOf(IOException.class);

        assertThat(home.resolve("evil.txt")).doesNotExist();
        assertThat(tmp.resolve("evil.txt")).doesNotExist();
    }

    @Test
    void should_fail_when_zip_has_multiple_top_level_folders() throws IOException {
        var entries = new LinkedHashMap<String, String>();
        entries.put("m1/a.txt", "a");
        entries.put("m2/b.txt", "b");
        var home = home();
        var zip = zip("m.zip", entries);

        assertThatThrownBy(() -> ModuleInstaller.install(zip, home))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("top-level");
    }
}
