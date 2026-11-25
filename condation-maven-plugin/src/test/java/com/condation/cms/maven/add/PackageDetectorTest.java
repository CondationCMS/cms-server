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

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageDetectorTest {

    @TempDir
    Path tmp;

    private void java(String pkg, String name, String body) throws IOException {
        var dir = tmp.resolve(pkg.replace('.', '/'));
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name + ".java"), "package " + pkg + ";\n\n" + body);
    }

    @Test
    void should_use_most_common_package_of_extension_classes() throws IOException {
        java("com.a.ext", "One", "@Extension(X.class) class One {}");
        java("com.a.ext", "Two", "@Extension(X.class) class Two {}");
        java("com.a.other", "Three", "@Extension(X.class) class Three {}");

        assertThat(PackageDetector.detect(tmp, "com.group")).isEqualTo("com.a.ext");
    }

    @Test
    void should_fall_back_to_group_id_when_no_extensions() throws IOException {
        java("com.a.util", "Helper", "class Helper {}");

        assertThat(PackageDetector.detect(tmp, "com.group")).isEqualTo("com.group");
    }

    @Test
    void should_ignore_files_without_extension_annotation() throws IOException {
        java("com.a.util", "Helper", "class Helper {}");
        java("com.a.util", "Helper2", "class Helper2 {}");
        java("com.a.ext", "One", "@Extension(X.class) class One {}");

        assertThat(PackageDetector.detect(tmp, "com.group")).isEqualTo("com.a.ext");
    }

    @Test
    void should_use_group_id_when_source_directory_missing() {
        assertThat(PackageDetector.detect(tmp.resolve("missing"), "com.group")).isEqualTo("com.group");
    }

    @Test
    void should_choose_alphabetically_first_package_on_tie() throws IOException {
        java("com.b", "One", "@Extension(X.class) class One {}");
        java("com.a", "Two", "@Extension(X.class) class Two {}");

        assertThat(PackageDetector.detect(tmp, "g")).isEqualTo("com.a");
    }

    @Test
    void should_make_group_id_a_valid_package() {
        assertThat(PackageDetector.detect(tmp.resolve("missing"), "com.my-company.cms")).isEqualTo("com.my_company.cms");
    }
}
