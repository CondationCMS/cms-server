package com.condation.cms.maven.add.types;

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

import static com.condation.cms.maven.add.types.TypeTestSupport.spec;
import static org.assertj.core.api.Assertions.assertThat;
import com.condation.cms.maven.add.ExtensionType;
import com.condation.cms.maven.add.ExtensionTypes;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FormattingTest {

    static Stream<ExtensionType> allTypes() {
        return ExtensionTypes.all().stream();
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void should_start_with_package_declaration_without_indentation(ExtensionType type) {
        assertThat(type.render(spec(type))).startsWith("package com.example.mod;\n");
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void should_indent_in_steps_of_four_spaces(ExtensionType type) {
        var lines = type.render(spec(type)).lines().filter(l -> !l.isBlank()).toList();

        assertThat(lines).allSatisfy(line -> {
            var indent = line.length() - line.stripLeading().length();
            assertThat(indent % 4).as("indent of: " + line).isZero();
        });
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void should_end_with_single_line_break_and_no_trailing_spaces(ExtensionType type) {
        var source = type.render(spec(type));

        assertThat(source).endsWith("}\n").doesNotEndWith("\n\n");
        assertThat(source.lines()).noneMatch(l -> l.endsWith(" "));
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void should_put_class_declaration_at_column_zero(ExtensionType type) {
        assertThat(type.render(spec(type)).lines()).anyMatch(l -> l.startsWith("public class MyExt "));
    }
}
