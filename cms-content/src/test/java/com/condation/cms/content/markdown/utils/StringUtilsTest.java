package com.condation.cms.content.markdown.utils;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.content.markdown.utils.StringUtils;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class StringUtilsTest {

	@ParameterizedTest
	@CsvSource({
		"\\!test\\!,&#33;test&#33;",
		"\\|test\\|,&#124;test&#124;",
		"\\.test\\.,&#46;test&#46;",
		"\\-test\\-,&#45;test&#45;",
		"\\+test\\+,&#43;test&#43;",
		"\\(test\\),&#40;test&#41;",
		"\\<test\\>,&#60;test&#62;",
		"\\[test\\],&#91;test&#93;",
		"\\{test\\},&#123;test&#125;",
		"\\#test\\#,&#35;test&#35;",
		"\\_test\\_,&#95;test&#95;",
		"\\`test\\`,&#96;test&#96;",
		"\\*test\\*,&#42;test&#42;"
	})
	void test_escape(final String input, final String expected) throws IOException {
		var result = StringUtils.escape(input);
		result = StringUtils.unescape(result);
		Assertions.assertThat(result).isEqualTo(expected);
	}
	
	 @Test
    void removeLeadingPipe_ShouldRemoveLeadingPipes() {
        // Test mit führenden Pipes
        String input = "||test";
        String result = StringUtils.removeLeadingPipe(input);
        Assertions.assertThat(result).isEqualTo("test");

        // Test ohne führende Pipes
        input = "test||";
        result = StringUtils.removeLeadingPipe(input);
        Assertions.assertThat(result).isEqualTo("test||");

        // Test mit nur Pipes
        input = "||||";
        result = StringUtils.removeLeadingPipe(input);
        Assertions.assertThat(result).isEqualTo("");  // Alles wird entfernt, da alle Pipes führend sind

        // Test mit leerem String
        input = "";
        result = StringUtils.removeLeadingPipe(input);
        Assertions.assertThat(result).isEqualTo("");

        // Test mit Pipes und Leerzeichen
        input = "|| test ||";
        result = StringUtils.removeLeadingPipe(input);
        Assertions.assertThat(result).isEqualTo(" test ||");
    }

    @Test
    void removeTrailingPipe_ShouldRemoveTrailingPipes() {
        // Test mit nachfolgenden Pipes
        String input = "test||";
        String result = StringUtils.removeTrailingPipe(input);
        Assertions.assertThat(result).isEqualTo("test");

        // Test ohne nachfolgende Pipes
        input = "||test";
        result = StringUtils.removeTrailingPipe(input);
        Assertions.assertThat(result).isEqualTo("||test");

        // Test mit nur Pipes
        input = "||||";
        result = StringUtils.removeTrailingPipe(input);
        Assertions.assertThat(result).isEqualTo("");  // Alles wird entfernt, da alle Pipes nachfolgend sind

        // Test mit leerem String
        input = "";
        result = StringUtils.removeTrailingPipe(input);
        Assertions.assertThat(result).isEqualTo("");

        // Test mit Pipes und Leerzeichen
        input = "|| test ||";
        result = StringUtils.removeTrailingPipe(input);
        Assertions.assertThat(result).isEqualTo("|| test ");
    }

}
