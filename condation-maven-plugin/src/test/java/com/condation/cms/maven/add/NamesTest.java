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
import java.util.List;
import org.junit.jupiter.api.Test;

class NamesTest {

    private static ExtensionType type(String id) {
        return new ExtensionType() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String label() {
                return id;
            }

            @Override
            public List<Question> questions() {
                return List.of();
            }

            @Override
            public String render(ExtensionSpec spec) {
                return "";
            }
        };
    }

    @Test
    void should_accept_valid_class_name() {
        assertThat(Names.requireClassName("MyTag")).isEqualTo("MyTag");
    }

    @Test
    void should_reject_when_class_name_starts_with_digit() {
        assertThatThrownBy(() -> Names.requireClassName("1Foo")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1Foo");
    }

    @Test
    void should_reject_when_class_name_contains_space() {
        assertThatThrownBy(() -> Names.requireClassName("foo bar")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_when_class_name_is_java_keyword() {
        assertThatThrownBy(() -> Names.requireClassName("class")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_empty_class_name() {
        assertThatThrownBy(() -> Names.requireClassName("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireClassName(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_identifier_for_parameter_name() {
        assertThat(Names.requireIdentifier("name")).isEqualTo("name");
    }

    @Test
    void should_reject_identifier_with_space_or_keyword() {
        assertThatThrownBy(() -> Names.requireIdentifier("my param")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireIdentifier("class")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_absolute_path_and_reject_relative() {
        assertThat(Names.requireAbsolutePath("/my-api/*")).isEqualTo("/my-api/*");
        assertThatThrownBy(() -> Names.requireAbsolutePath("my-api")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("my-api");
        assertThatThrownBy(() -> Names.requireAbsolutePath("/a b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireAbsolutePath("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_relative_path_and_reject_leading_slash_parent_reference_query_and_backslash() {
        assertThat(Names.requireRelativePath("chat/room")).isEqualTo("chat/room");
        assertThatThrownBy(() -> Names.requireRelativePath("/x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireRelativePath("a/../b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireRelativePath("a?b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireRelativePath("a\\b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Names.requireRelativePath(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_nested_package() {
        assertThat(Names.requirePackage("com.example.my_module")).isEqualTo("com.example.my_module");
    }

    @Test
    void should_reject_package_with_keyword_segment() {
        assertThatThrownBy(() -> Names.requirePackage("com.int.x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_escape_quotes_and_backslashes_in_java_string() {
        assertThat(Names.javaString("a\"b\\c")).isEqualTo("a\\\"b\\\\c");
    }

    @Test
    void should_escape_line_breaks_in_java_string() {
        assertThat(Names.javaString("a\nb")).isEqualTo("a\\nb");
    }

    @Test
    void should_build_default_class_name_from_type_id() {
        assertThat(Names.defaultClassName(type("template-function"))).isEqualTo("MyTemplateFunctionExtension");
        assertThat(Names.defaultClassName(type("hook"))).isEqualTo("MyHookExtension");
    }
}
