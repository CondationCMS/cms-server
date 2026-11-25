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
import org.jline.terminal.impl.DumbTerminal;
import org.junit.jupiter.api.Test;

class JLinePrompterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private JLinePrompter prompter(String input) throws IOException {
        // a plain stream terminal: no pty, deterministic input
        var terminal = new DumbTerminal(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out);
        return new JLinePrompter(terminal);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void should_return_default_when_enter_pressed() throws IOException {
        try (var prompter = prompter("\n")) {
            assertThat(prompter.ask("Name", "hello", s -> true, "bad")).isEqualTo("hello");
        }
        assertThat(output()).contains("Name").contains("[hello]");
    }

    @Test
    void should_return_typed_value() throws IOException {
        try (var prompter = prompter("my-tag\n")) {
            assertThat(prompter.ask("Name", "hello", s -> true, "bad")).isEqualTo("my-tag");
        }
    }

    @Test
    void should_ask_again_when_validator_rejects() throws IOException {
        try (var prompter = prompter("bad\ngood\n")) {
            assertThat(prompter.ask("Name", "x", s -> s.equals("good"), "Not allowed")).isEqualTo("good");
        }
        assertThat(output()).contains("Not allowed");
    }

    @Test
    void should_return_selected_choice_by_name_ignoring_case() throws IOException {
        try (var prompter = prompter("websocket\n")) {
            assertThat(prompter.select("Transport", List.of("SSE", "WEBSOCKET"), "SSE")).isEqualTo("WEBSOCKET");
        }
    }

    @Test
    void should_return_selected_choice_by_number() throws IOException {
        try (var prompter = prompter("2\n")) {
            assertThat(prompter.select("Transport", List.of("SSE", "WEBSOCKET"), "SSE")).isEqualTo("WEBSOCKET");
        }
        assertThat(output()).contains("1) SSE").contains("2) WEBSOCKET");
    }

    @Test
    void should_return_default_choice_when_enter_pressed() throws IOException {
        try (var prompter = prompter("\n")) {
            assertThat(prompter.select("Transport", List.of("SSE", "WEBSOCKET"), "SSE")).isEqualTo("SSE");
        }
    }

    @Test
    void should_ask_again_when_choice_unknown() throws IOException {
        try (var prompter = prompter("foo\n7\nSSE\n")) {
            assertThat(prompter.select("Transport", List.of("SSE", "WEBSOCKET"), "SSE")).isEqualTo("SSE");
        }
        assertThat(output()).contains("Please choose one of");
    }

    @Test
    void should_throw_abort_when_end_of_input() throws IOException {
        try (var prompter = prompter("")) {
            assertThatThrownBy(() -> prompter.ask("Name", "x", s -> true, "bad")).isInstanceOf(UserAbortException.class);
        }
    }

    @Test
    void should_throw_abort_when_end_of_input_during_select() throws IOException {
        try (var prompter = prompter("")) {
            assertThatThrownBy(() -> prompter.select("T", List.of("A", "B"), "A")).isInstanceOf(UserAbortException.class);
        }
    }

    @Test
    void should_confirm_with_default_when_enter_pressed() throws IOException {
        try (var prompter = prompter("\n\n")) {
            assertThat(prompter.confirm("Overwrite?", false)).isFalse();
            assertThat(prompter.confirm("Overwrite?", true)).isTrue();
        }
    }

    @Test
    void should_confirm_yes_and_no() throws IOException {
        try (var prompter = prompter("y\nN\nyes\n")) {
            assertThat(prompter.confirm("Overwrite?", false)).isTrue();
            assertThat(prompter.confirm("Overwrite?", true)).isFalse();
            assertThat(prompter.confirm("Overwrite?", false)).isTrue();
        }
    }

    @Test
    void should_ask_again_when_confirmation_unclear() throws IOException {
        try (var prompter = prompter("maybe\ny\n")) {
            assertThat(prompter.confirm("Overwrite?", false)).isTrue();
        }
    }

    @Test
    void should_prefer_exec_provider_on_unix_to_avoid_native_access_warnings() {
        assertThat(JLinePrompter.preferredProviders("Mac OS X")).containsExactly("exec");
        assertThat(JLinePrompter.preferredProviders("Linux")).containsExactly("exec");
    }

    @Test
    void should_use_default_providers_on_windows() {
        assertThat(JLinePrompter.preferredProviders("Windows 11")).isEmpty();
        assertThat(JLinePrompter.preferredProviders(null)).isEmpty();
    }
}
