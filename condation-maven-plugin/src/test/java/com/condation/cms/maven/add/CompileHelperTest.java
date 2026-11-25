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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class CompileHelperTest {

    @Test
    void should_fail_when_source_does_not_compile() {
        assertThatThrownBy(() -> CompileHelper.compile("a", "B", "package a; public class B { int x = \"no\"; }"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("does not compile");
    }

    @Test
    void should_fail_when_cms_api_class_missing_from_classpath_is_used_wrongly() {
        assertThatThrownBy(() -> CompileHelper.compile("a", "B",
                "package a; import com.condation.cms.api.annotations.ShortCode; public class B { @ShortCode(\"x\", bogus = 1) void m() {} }"))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void should_compile_valid_source_using_cms_api() {
        assertThatCode(() -> CompileHelper.compile("a", "B",
                "package a; import com.condation.cms.api.annotations.ShortCode; public class B { @ShortCode(\"x\") void m() {} }"))
                .doesNotThrowAnyException();
    }
}
