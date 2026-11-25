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

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;
import org.jline.utils.InfoCmp.Capability;

/**
 * Interactive prompts on a JLine terminal: defaults on enter, tab completion for choices.
 */
public class JLinePrompter implements Prompter, AutoCloseable {

    private final Terminal terminal;
    private final LineReader reader;
    private volatile List<String> completions = List.of();

    public JLinePrompter(Terminal terminal) {
        this.terminal = terminal;
        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer((r, line, candidates) -> completions.stream()
                        .filter(c -> c.regionMatches(true, 0, line.word(), 0, line.word().length()))
                        .forEach(c -> candidates.add(new Candidate(c))))
                .build();
    }

    /** @return true if stdin and stdout are connected to a terminal */
    public static boolean terminalAvailable() {
        var console = System.console();
        return console != null && console.isTerminal();
    }

    public static JLinePrompter system() throws IOException {
        return new JLinePrompter(systemTerminal());
    }

    /**
     * On Unix the <code>exec</code> provider (stty) is enough and, unlike the FFM and JNI providers, does not
     * need native access (JDK 24+ prints a warning for those). Windows needs the native providers.
     */
    static List<String> preferredProviders(String osName) {
        return osName != null && !osName.toLowerCase().contains("win") ? List.of("exec") : List.of();
    }

    private static Terminal systemTerminal() throws IOException {
        for (var provider : preferredProviders(System.getProperty("os.name"))) {
            try {
                return TerminalBuilder.builder().system(true).provider(provider).build();
            } catch (IOException | RuntimeException _) {
                // try the next provider, finally the default selection
            }
        }
        return TerminalBuilder.builder().system(true).build();
    }

    /** Dumb terminals cannot send arrow keys or redraw, they get the numbered text prompt. */
    static boolean supportsArrowSelection(String terminalType) {
        return terminalType != null && !terminalType.startsWith("dumb");
    }

    @Override
    public String select(String label, List<String> choices, String defaultValue) {
        if (supportsArrowSelection(terminal.getType())) {
            return arrowSelect(label, choices, defaultValue);
        }
        return textSelect(label, choices, defaultValue);
    }

    private enum Key { UP, DOWN, HOME, END, ENTER, ABORT, IGNORE }

    private static KeyMap<Key> selectionKeys() {
        var keys = new KeyMap<Key>();
        keys.bind(Key.UP, "\033[A", "\033OA", KeyMap.ctrl('P'), "k");
        keys.bind(Key.DOWN, "\033[B", "\033OB", KeyMap.ctrl('N'), "j");
        keys.bind(Key.HOME, "\033[H", "\033OH", "\033[1~");
        keys.bind(Key.END, "\033[F", "\033OF", "\033[4~");
        keys.bind(Key.ENTER, "\r", "\n");
        keys.bind(Key.ABORT, KeyMap.ctrl('C'), KeyMap.ctrl('D'));
        keys.setNomatch(Key.IGNORE);
        keys.setUnicode(Key.IGNORE);
        return keys;
    }

    /** List with a highlighted entry, moved with arrow up/down, confirmed with enter. */
    private String arrowSelect(String label, List<String> choices, String defaultValue) {
        var keys = selectionKeys();
        var bindings = new BindingReader(terminal.reader());
        var display = new Display(terminal, false);
        var cursor = Math.max(0, choices.indexOf(defaultValue));
        var previous = terminal.enterRawMode();
        terminal.puts(Capability.cursor_invisible);
        terminal.flush();
        try {
            while (true) {
                var size = terminal.getSize();
                var rows = size.getRows() > 0 ? size.getRows() : 24;
                display.resize(rows, size.getColumns() > 0 ? size.getColumns() : 80);
                display.update(selectionLines(label, choices, cursor, rows), 0);

                var key = bindings.readBinding(keys);
                if (key == null || key == Key.ABORT) {
                    display.update(List.of(), 0);
                    throw new UserAbortException();
                }
                switch (key) {
                    case UP -> cursor = (cursor - 1 + choices.size()) % choices.size();
                    case DOWN -> cursor = (cursor + 1) % choices.size();
                    case HOME -> cursor = 0;
                    case END -> cursor = choices.size() - 1;
                    case ENTER -> {
                        display.update(List.of(), 0);
                        println(label + ": " + choices.get(cursor));
                        return choices.get(cursor);
                    }
                    default -> {
                        // ignore other keys
                    }
                }
            }
        } catch (EndOfFileException | IOError _) {
            throw new UserAbortException();
        } finally {
            terminal.puts(Capability.cursor_normal);
            terminal.flush();
            terminal.setAttributes(previous);
        }
    }

    private static List<AttributedString> selectionLines(String label, List<String> choices, int cursor, int rows) {
        // header and hint use two rows, the rest shows a window around the cursor
        var visible = Math.clamp(choices.size(), 3, Math.max(3, rows - 3));
        var start = Math.clamp(cursor - visible / 2, 0, Math.max(0, choices.size() - visible));
        var lines = new java.util.ArrayList<AttributedString>();
        lines.add(new AttributedStringBuilder()
                .style(AttributedStyle.BOLD).append("? " + label).toAttributedString());
        for (int i = start; i < start + visible && i < choices.size(); i++) {
            var line = new AttributedStringBuilder();
            if (i == cursor) {
                line.style(AttributedStyle.BOLD.foreground(AttributedStyle.CYAN)).append("> " + choices.get(i));
            } else {
                line.append("  " + choices.get(i));
            }
            lines.add(line.toAttributedString());
        }
        var hint = "up/down: move, enter: select, ctrl+c: abort"
                + (visible < choices.size() ? "  (" + (cursor + 1) + "/" + choices.size() + ")" : "");
        lines.add(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.faint()).append(hint).toAttributedString());
        return lines;
    }

    private String textSelect(String label, List<String> choices, String defaultValue) {
        for (int i = 0; i < choices.size(); i++) {
            terminal.writer().println("  " + (i + 1) + ") " + choices.get(i));
        }
        terminal.writer().flush();
        completions = choices;
        try {
            while (true) {
                var input = read(label + " [" + defaultValue + "]: ").trim();
                if (input.isEmpty()) {
                    return defaultValue;
                }
                var match = match(choices, input);
                if (match != null) {
                    return match;
                }
                println("Please choose one of: " + String.join(", ", choices) + " (or the number)");
            }
        } finally {
            completions = List.of();
        }
    }

    private static String match(List<String> choices, String input) {
        if (input.chars().allMatch(Character::isDigit)) {
            var index = Integer.parseInt(input) - 1;
            return index >= 0 && index < choices.size() ? choices.get(index) : null;
        }
        return choices.stream().filter(c -> c.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    @Override
    public String ask(String label, String defaultValue, Predicate<String> validator, String errorMessage) {
        while (true) {
            var input = read(label + " [" + defaultValue + "]: ").trim();
            var value = input.isEmpty() ? defaultValue : input;
            if (validator.test(value)) {
                return value;
            }
            println(errorMessage);
        }
    }

    @Override
    public boolean confirm(String label, boolean defaultValue) {
        var hint = defaultValue ? "[Y/n]" : "[y/N]";
        while (true) {
            var input = read(label + " " + hint + ": ").trim().toLowerCase();
            switch (input) {
                case "" -> {
                    return defaultValue;
                }
                case "y", "yes" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> println("Please answer y or n");
            }
        }
    }

    private String read(String prompt) {
        try {
            return reader.readLine(prompt);
        } catch (UserInterruptException | EndOfFileException _) {
            throw new UserAbortException();
        }
    }

    private void println(String text) {
        terminal.writer().println(text);
        terminal.writer().flush();
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }
}
