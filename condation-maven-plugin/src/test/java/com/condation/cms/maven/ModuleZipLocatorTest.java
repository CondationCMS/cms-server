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
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModuleZipLocatorTest {

    @TempDir
    Path target;

    @Test
    void should_prefer_explicit_zip_when_configured() throws IOException {
        var explicit = target.resolve("x.zip");
        Files.createFile(target.resolve("a-bin.zip"));

        assertThat(ModuleZipLocator.locate(explicit, target.resolve("attached-bin.zip"), target)).isEqualTo(explicit);
    }

    @Test
    void should_use_attached_zip_when_no_explicit_zip() throws IOException {
        var attached = target.resolve("attached-bin.zip");
        Files.createFile(attached);
        Files.createFile(target.resolve("a-bin.zip"));

        assertThat(ModuleZipLocator.locate(null, attached, target)).isEqualTo(attached);
    }

    @Test
    void should_find_single_bin_zip_in_target_when_nothing_configured() throws IOException {
        var zip = Files.createFile(target.resolve("example-module-bin.zip"));
        Files.createFile(target.resolve("example-module-8.4.0.jar"));

        assertThat(ModuleZipLocator.locate(null, null, target)).isEqualTo(zip);
    }

    @Test
    void should_fail_with_hint_when_no_zip_found() {
        assertThatThrownBy(() -> ModuleZipLocator.locate(null, null, target))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("mvn package");
    }

    @Test
    void should_fail_when_multiple_bin_zips_found() throws IOException {
        Files.createFile(target.resolve("a-bin.zip"));
        Files.createFile(target.resolve("b-bin.zip"));

        assertThatThrownBy(() -> ModuleZipLocator.locate(null, null, target))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("moduleZip");
    }
}
