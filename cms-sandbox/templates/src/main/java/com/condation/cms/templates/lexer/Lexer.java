package com.condation.cms.templates.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position = 0;
    private boolean inTag = false; // neuer Zustand

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            char c = input.charAt(position);

            if (c == '{' && peek(1) == '{') {
                tokens.add(new Token(Token.Type.VARIABLE, "{{"));
                position += 2;
                inTag = true; // Wir sind jetzt in einer Variablen
            } else if (c == '{' && peek(1) == '%') {
                tokens.add(new Token(Token.Type.TAG_START, "{%"));
                position += 2;
                inTag = true; // Wir sind jetzt in einem Tag
            } else if (inTag && c == '%' && peek(1) == '}') {
                tokens.add(new Token(Token.Type.TAG_END, "%}"));
                position += 2;
                inTag = false; // Tag endet hier
            } else if (inTag && c == '}' && peek(1) == '}') {
                tokens.add(new Token(Token.Type.TAG_END, "}}"));
                position += 2;
                inTag = false; // Variable endet hier
            } else if (inTag && Character.isLetter(c)) {
                // Nur wenn wir im Tag sind, identifizieren wir einen IDENTIFIER
                tokens.add(new Token(Token.Type.IDENTIFIER, readWhile(Character::isLetter)));
            } else {
                tokens.add(new Token(Token.Type.TEXT, String.valueOf(c)));
                position++;
            }
        }
        tokens.add(new Token(Token.Type.END, ""));
        return tokens;
    }

    private char peek(int offset) {
        return (position + offset < input.length()) ? input.charAt(position + offset) : '\0';
    }

    private String readWhile(java.util.function.Predicate<Character> condition) {
        StringBuilder result = new StringBuilder();
        while (position < input.length() && condition.test(input.charAt(position))) {
            result.append(input.charAt(position++));
        }
        return result.toString();
    }
}
