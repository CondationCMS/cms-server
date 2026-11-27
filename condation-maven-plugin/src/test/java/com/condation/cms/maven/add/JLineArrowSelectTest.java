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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import org.jline.terminal.Size;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

/**
 * Arrow key selection on a terminal that supports keys (xterm), keys are sent as the escape sequences
 * a real terminal sends.
 */
class JLineArrowSelectTest {

    private static final String UP = "\u001b[A";
    private static final String DOWN = "\u001b[B";
    private static final String APP_DOWN = "\u001bOB";
    private static final String ENTER = "\r";
    private static final String CTRL_C = "\u0003";

    private static final List<String> CHOICES = List.of("hook", "shortcode", "realtime", "routes");

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private JLinePrompter prompter(String keys, int rows) throws IOException {
        var terminal = TerminalBuilder.builder()
                .system(false)
                .provider("exec")
                .type("xterm-256color")
                .streams(new ByteArrayInputStream(keys.getBytes(StandardCharsets.UTF_8)), out)
                .build();
        terminal.setSize(new Size(80, rows));
        return new JLinePrompter(terminal);
    }

    private String select(String keys) throws IOException {
        try (var prompter = prompter(keys, 24)) {
            return prompter.select("Extension type", CHOICES, "hook");
        }
    }

    @Test
    void should_return_default_when_only_enter_pressed() throws IOException {
        assertThat(select(ENTER)).isEqualTo("hook");
    }

    @Test
    void should_start_on_the_default_choice() throws IOException {
        try (var prompter = prompter(ENTER, 24)) {
            assertThat(prompter.select("Extension type", CHOICES, "realtime")).isEqualTo("realtime");
        }
    }

    @Test
    void should_move_down_with_arrow_down() throws IOException {
        assertThat(select(DOWN + ENTER)).isEqualTo("shortcode");
        assertThat(select(DOWN + DOWN + ENTER)).isEqualTo("realtime");
    }

    @Test
    void should_move_up_with_arrow_up() throws IOException {
        assertThat(select(DOWN + DOWN + UP + ENTER)).isEqualTo("shortcode");
    }

    @Test
    void should_wrap_around_at_both_ends() throws IOException {
        assertThat(select(UP + ENTER)).isEqualTo("routes");
        assertThat(select(DOWN + DOWN + DOWN + DOWN + ENTER)).isEqualTo("hook");
    }

    @Test
    void should_accept_application_mode_arrow_keys() throws IOException {
        assertThat(select(APP_DOWN + ENTER)).isEqualTo("shortcode");
    }

    @Test
    void should_accept_line_feed_as_enter() throws IOException {
        assertThat(select(DOWN + "\n")).isEqualTo("shortcode");
    }

    @Test
    void should_abort_on_ctrl_c() throws IOException {
        try (var prompter = prompter(DOWN + CTRL_C, 24)) {
            assertThatThrownBy(() -> prompter.select("T", CHOICES, "hook")).isInstanceOf(UserAbortException.class);
        }
    }

    @Test
    void should_abort_on_end_of_input() throws IOException {
        try (var prompter = prompter("", 24)) {
            assertThatThrownBy(() -> prompter.select("T", CHOICES, "hook")).isInstanceOf(UserAbortException.class);
        }
    }

    @Test
    void should_show_all_choices_and_the_result() throws IOException {
        select(DOWN + ENTER);

        var output = out.toString(StandardCharsets.UTF_8);
        assertThat(output).contains("Extension type").contains("hook").contains("shortcode").contains("realtime")
                .contains("routes");
    }

    @Test
    void should_scroll_when_list_is_longer_than_terminal() throws IOException {
        var many = IntStream.rangeClosed(1, 19).mapToObj(i -> "type-" + i).toList();
        var keys = DOWN.repeat(14) + ENTER;

        try (var prompter = prompter(keys, 8)) {
            assertThat(prompter.select("Extension type", many, "type-1")).isEqualTo("type-15");
        }
    }

    @Test
    void should_work_on_very_small_terminals_and_short_lists() throws IOException {
        try (var prompter = prompter(DOWN + ENTER, 3)) {
            assertThat(prompter.select("Extension type", CHOICES, "hook")).isEqualTo("shortcode");
        }
        try (var prompter = prompter(DOWN + ENTER, 24)) {
            assertThat(prompter.select("Extension type", List.of("a", "b"), "a")).isEqualTo("b");
        }
    }

    @Test
    void should_still_accept_typed_names_on_dumb_terminals() {
        // covered in JLinePrompterTest: dumb terminals keep the numbered text prompt
        assertThat(JLinePrompter.supportsArrowSelection("dumb")).isFalse();
        assertThat(JLinePrompter.supportsArrowSelection("dumb-color")).isFalse();
        assertThat(JLinePrompter.supportsArrowSelection("xterm-256color")).isTrue();
        assertThat(JLinePrompter.supportsArrowSelection(null)).isFalse();
    }
}
