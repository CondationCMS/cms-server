package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
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
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MarkdownHelperTest {

    @Test
    void shouldReplaceMiddleSection() {
        String markdown = "Hello World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                6,
                11,
                "CMS");

        assertEquals("Hello CMS", result);
    }

    @Test
    void shouldReplaceAtBeginning() {
        String markdown = "Hello World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                0,
                5,
                "Hi");

        assertEquals("Hi World", result);
    }

    @Test
    void shouldReplaceAtEnd() {
        String markdown = "Hello World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                6,
                11,
                "Universe");

        assertEquals("Hello Universe", result);
    }

    @Test
    void shouldReplaceWholeString() {
        String markdown = "Hello World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                0,
                markdown.length(),
                "New Content");

        assertEquals("New Content", result);
    }

    @Test
    void shouldInsertAtBeginning() {
        String markdown = "World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                0,
                0,
                "Hello ");

        assertEquals("Hello World", result);
    }

    @Test
    void shouldInsertAtEnd() {
        String markdown = "Hello";

        String result = MarkdownHelper.replaceRange(
                markdown,
                markdown.length(),
                markdown.length(),
                " World");

        assertEquals("Hello World", result);
    }

    @Test
    void shouldInsertInMiddle() {
        String markdown = "HelloWorld";

        String result = MarkdownHelper.replaceRange(
                markdown,
                5,
                5,
                " ");

        assertEquals("Hello World", result);
    }

    @Test
    void shouldRemoveSection() {
        String markdown = "Hello World";

        String result = MarkdownHelper.replaceRange(
                markdown,
                5,
                11,
                "");

        assertEquals("Hello", result);
    }

    @Test
    void shouldReplaceMarkdownImage() {
        String markdown = """
                # Article

                ![Team](/images/team.jpg)

                Some text.
                """;

        String oldImage = "![Team](/images/team.jpg)";
        int start = markdown.indexOf(oldImage);
        int end = start + oldImage.length();

        String result = MarkdownHelper.replaceRange(
                markdown,
                start,
                end,
                "![Team](/images/new-team.jpg)");

        assertTrue(result.contains("![Team](/images/new-team.jpg)"));
        assertFalse(result.contains("![Team](/images/team.jpg)"));
    }

    @Test
    void shouldThrowForNegativeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MarkdownHelper.replaceRange(
                        "test",
                        -1,
                        2,
                        "x"));
    }

    @Test
    void shouldThrowWhenEndBeforeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MarkdownHelper.replaceRange(
                        "test",
                        3,
                        2,
                        "x"));
    }

    @Test
    void shouldThrowWhenEndExceedsLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MarkdownHelper.replaceRange(
                        "test",
                        0,
                        10,
                        "x"));
    }

    @Test
    void shouldThrowForNullMarkdown() {
        assertThrows(
                NullPointerException.class,
                () -> MarkdownHelper.replaceRange(
                        null,
                        0,
                        0,
                        "x"));
    }

    @Test
    void shouldThrowForNullReplacement() {
        assertThrows(
                NullPointerException.class,
                () -> MarkdownHelper.replaceRange(
                        "test",
                        0,
                        0,
                        null));
    }

    @Test
    void shouldReplaceSimpleMediaImage() {
        String md = "![img](/media/images/test.jpg)";

        String result = MarkdownHelper.replaceImage(
                "/",
                md,
                0,
                md.length(),
                "testimg.jpg"
        );

        Assertions.assertThat(result).isEqualTo("![img](/media/testimg.jpg)");
    }

    @Test
    void shouldReplaceSimpleMediaImageWithFormat() {
        String md = "![img](/media/images/test.jpg?format=small)";

        String result = MarkdownHelper.replaceImage(
                "/",
                md,
                0,
                md.length(),
                "testimg.jpg"
        );

        Assertions.assertThat(result).isEqualTo("![img](/media/testimg.jpg?format=small)");
    }

    @Test
    void shouldReplaceSimpleMediaImageWithContextPath() {
        String md = "![img](/de/media/images/test.jpg)";

        String result = MarkdownHelper.replaceImage(
                "/de",
                md,
                0,
                md.length(),
                "testimg.jpg"
        );

        Assertions.assertThat(result).isEqualTo("![img](/de/media/testimg.jpg)");
    }

}
