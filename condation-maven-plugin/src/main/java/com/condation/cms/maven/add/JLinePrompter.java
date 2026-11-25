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

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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
        return new JLinePrompter(TerminalBuilder.builder().system(true).build());
    }

    @Override
    public String select(String label, List<String> choices, String defaultValue) {
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
        } catch (UserInterruptException | EndOfFileException e) {
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
