package com.condation.cms.filesystem.metadata.query.parser;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String input;
    private int pos = 0;

    public Tokenizer(String input) {
        this.input = input;
    }

    public static List<Token> tokenize (String input) {
        return new Tokenizer(input).tokenize();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "("));
                pos++;
                continue;
            }
            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")"));
                pos++;
                continue;
            }
            if (c == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                pos++;
                continue;
            }

            if (c == '\'' || c == '"') { // String literal
                tokens.add(new Token(TokenType.STRING, readString(c)));
                continue;
            }

            if (Character.isDigit(c)) { // Number literal
                tokens.add(new Token(TokenType.NUMBER, readNumber()));
                continue;
            }

            // Allow identifiers to start with letter OR underscore
            if (Character.isLetter(c) || c == '_') {
                String word = readWord();

                switch (word.toUpperCase()) {
                    case "AND" -> tokens.add(new Token(TokenType.AND, word));
                    case "OR" -> tokens.add(new Token(TokenType.OR, word));
                    case "TRUE", "FALSE" -> tokens.add(new Token(TokenType.BOOLEAN, word.toLowerCase()));
                    case "IN", "NOT", "CONTAINS" -> {
                        // später schauen, ob zusammengesetzt
                        tokens.add(new Token(TokenType.OPERATOR, word.toUpperCase()));
                    }
                    default -> tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
                continue;
            }

            if (isOperatorStart(c)) {
                tokens.add(new Token(TokenType.OPERATOR, readOperator()));
                continue;
            }

            throw new RuntimeException("Unexpected character: " + c);
        }

        // zusammengesetzte Operatoren zusammenfassen
        List<Token> merged = mergeMultiWordOperators(tokens);

        merged.add(new Token(TokenType.EOF, ""));
        return merged;
    }

    private String readString(char quote) {
        pos++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && input.charAt(pos) != quote) {
            sb.append(input.charAt(pos++));
        }
        pos++; // skip closing quote
        return sb.toString();
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() &&
                (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            sb.append(input.charAt(pos++));
        }
        return sb.toString();
    }

    // accept letters, digits, underscore and hyphen in identifiers (e.g. number2, my_var)
    private String readWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private boolean isOperatorStart(char c) {
        return "=!<>".indexOf(c) >= 0;
    }

    private String readOperator() {
        if (pos + 1 < input.length()) {
            String two = input.substring(pos, pos + 2);
            if (two.equals("!=") || two.equals("<=") || two.equals(">=")) {
                pos += 2;
                return two;
            }
        }
        return String.valueOf(input.charAt(pos++));
    }

    private List<Token> mergeMultiWordOperators(List<Token> tokens) {
        List<Token> merged = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            if (t.type() == TokenType.OPERATOR && t.text().equalsIgnoreCase("NOT")) {
                if (i + 1 < tokens.size() && tokens.get(i + 1).text().equalsIgnoreCase("IN")) {
                    merged.add(new Token(TokenType.OPERATOR, "NOT IN"));
                    i++;
                    continue;
                }
            }
            if (t.type() == TokenType.OPERATOR && t.text().equalsIgnoreCase("CONTAINS")) {
                if (i + 1 < tokens.size() && tokens.get(i + 1).text().equalsIgnoreCase("NOT")) {
                    merged.add(new Token(TokenType.OPERATOR, "CONTAINS NOT"));
                    i++;
                    continue;
                }
            }

            merged.add(t);
        }
        return merged;
    }
}
