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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExtensionGeneratorTest {

    @TempDir
    Path tmp;

    private final ExtensionType type = ExtensionTypes.byId("shortcode").orElseThrow();

    private ExtensionSpec spec(String packageName, String className) {
        return new ExtensionSpec(packageName, className, Map.of("name", "hello", "param", "name"));
    }

    @Test
    void should_write_file_into_package_directory() throws IOException {
        var file = ExtensionGenerator.write(tmp, type, spec("com.example.mod", "HelloTag"), false);

        assertThat(file).isEqualTo(tmp.resolve("com/example/mod/HelloTag.java")).content()
                .contains("package com.example.mod;").contains("class HelloTag");
    }

    @Test
    void should_create_missing_source_directory() throws IOException {
        var source = tmp.resolve("src/main/java");

        ExtensionGenerator.write(source, type, spec("a.b", "X"), false);

        assertThat(source.resolve("a/b/X.java")).exists();
    }

    @Test
    void should_fail_when_file_exists_and_overwrite_false() throws IOException {
        ExtensionGenerator.write(tmp, type, spec("a.b", "X"), false);
        Files.writeString(tmp.resolve("a/b/X.java"), "mine");

        assertThatThrownBy(() -> ExtensionGenerator.write(tmp, type, spec("a.b", "X"), false))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(tmp.resolve("a/b/X.java")).hasContent("mine");
    }

    @Test
    void should_overwrite_when_flag_set() throws IOException {
        ExtensionGenerator.write(tmp, type, spec("a.b", "X"), false);
        Files.writeString(tmp.resolve("a/b/X.java"), "mine");

        ExtensionGenerator.write(tmp, type, spec("a.b", "X"), true);

        assertThat(tmp.resolve("a/b/X.java")).content().contains("class X");
    }

    @Test
    void should_reject_invalid_class_name_without_writing() throws IOException {
        var badClassName = spec("a.b", "1Bad");
        var badPackage = spec("a.class", "Ok");

        assertThatThrownBy(() -> ExtensionGenerator.write(tmp, type, badClassName, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ExtensionGenerator.write(tmp, type, badPackage, false))
                .isInstanceOf(IllegalArgumentException.class);

        try (var files = Files.list(tmp)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void should_not_leave_partial_file_when_rendering_fails() {
        var bad = new ExtensionSpec("a.b", "X", Map.of("name", "hello", "param", "my param"));

        assertThatThrownBy(() -> ExtensionGenerator.write(tmp, type, bad, false))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(tmp.resolve("a/b/X.java")).doesNotExist();
    }

    @Test
    void should_report_whether_target_exists() {
        assertThat(ExtensionGenerator.target(tmp, spec("a.b", "X"))).isEqualTo(tmp.resolve("a/b/X.java"));
    }
}
