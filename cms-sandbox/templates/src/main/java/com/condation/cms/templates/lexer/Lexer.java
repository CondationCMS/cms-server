package com.condation.cms.templates.lexer;

import com.condation.cms.templates.parser.TokenStream;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

	private final String input;
	private int position = 0;

	private int line = 1;
	private int column = 1;// neuer Zustand

	private final State state = new State();
	
	public Lexer(String input) {
		this.input = input;
	}

	public TokenStream tokenize() {
		List<Token> tokens = new ArrayList<>();
		while (position < input.length()) {
			char c = input.charAt(position);

			if (c == '{' && peek(1) == '{') {
				tokens.add(new Token(Token.Type.VARIABLE_START, "{{", line, column));
				position += 2;
				state.set(State.Type.VARIABLE);
			} else if (c == '{' && peek(1) == '%') {
				tokens.add(new Token(Token.Type.TAG_START, "{%", line, column));
				position += 2;
				state.set(State.Type.TAG);
				readTagContent(tokens); // Inhalte des Tags lesen
			} else if (c == '{' && peek(1) == '#') {
				tokens.add(new Token(Token.Type.COMMENT_START, "{*", line, column));
				position += 2;
				state.set(State.Type.COMMENT);
			} else if (state.is(State.Type.TAG) && c == '%' && peek(1) == '}') {
				tokens.add(new Token(Token.Type.TAG_END, "%}", line, column));
				position += 2;
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE) && c == '}' && peek(1) == '}') {
				tokens.add(new Token(Token.Type.VARIABLE_END, "}}", line, column));
				position += 2;
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.COMMENT) && c == '#' && peek(1) == '}') {
				tokens.add(new Token(Token.Type.COMMENT_END, "#}", line, column));
				position += 2;
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE, State.Type.TAG) && Character.isLetter(c)) {
				// Nur wenn wir im Tag sind, identifizieren wir einen IDENTIFIER
				tokens.add(new Token(Token.Type.IDENTIFIER, readWhile(Character::isLetter), line, column));
			} else if (state.is(State.Type.COMMENT)) {
				tokens.add(new Token(Token.Type.COMMENT_VALUE, readUntil("#}"), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else if (!state.is(State.Type.VARIABLE, State.Type.TAG)) {
				tokens.add(new Token(Token.Type.TEXT, readUntil("{"), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else {
				advance();
			}
		}
		tokens.add(new Token(Token.Type.END, "", line, column));
		return new TokenStream(tokens);
	}

	private void readTagContent(List<Token> tokens) {
		skipWhitespace();

		String keyword = readWhile(Character::isLetter);
		tokens.add(new Token(Token.Type.IDENTIFIER, keyword, line, column));

		String condition = readUntil("%");
		tokens.add(new Token(Token.Type.EXPRESSION, condition, line, column));
	}

	private char peek(int offset) {
		return (position + offset < input.length()) ? input.charAt(position + offset) : '\0';
	}

	private String readWhile(java.util.function.Predicate<Character> condition) {
		StringBuilder result = new StringBuilder();
		while (position < input.length() && condition.test(input.charAt(position))) {
			result.append(input.charAt(position));
			
			advance();
		}
		return result.toString();
	}

	private String readUntil(String delimiter) {
		StringBuilder result = new StringBuilder();
		while (position < input.length() && !input.startsWith(delimiter, position)) {
			result.append(input.charAt(position));
			advance();
		}
		return result.toString();
	}

	private void skipWhitespace() {
		while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
			advance();
		}
	}

	private void advance() {
		if (input.charAt(position) == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}
		position++;
	}
}
