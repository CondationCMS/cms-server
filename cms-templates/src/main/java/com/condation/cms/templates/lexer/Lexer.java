package com.condation.cms.templates.lexer;

/*-
 * #%L
 * templates
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
import java.util.ArrayList;
import java.util.List;

public class Lexer {

	public class Syntax {
		public static final String TAG_OPEN = "{%";
		public static final String TAG_CLOSE = "%}";
		
		public static final String COMMENT_OPEN = "{#";
		public static final String COMMENT_CLOSE = "#}";
		
		public static final String VARIABLE_OPEN = "{{";
		public static final String VARIABLE_CLOSE = "}}";
		
		public static final String COMPONENT_OPEN = "{[";
		public static final String COMPONENT_CLOSE = "]}";
		
		public static final String[] OPENING = new String[] {
			TAG_OPEN, COMMENT_OPEN, VARIABLE_OPEN, COMPONENT_OPEN};
		
		public static final String[] CLOSING = new String[] {
			TAG_CLOSE, COMMENT_CLOSE, VARIABLE_CLOSE, COMPONENT_CLOSE};
	}
	
	public Lexer() {

	}

	public TokenStream tokenize(String input) {

		CharacterStream charStream = new CharacterStream(input);
//		ReaderCharacterStream charStream = new ReaderCharacterStream(new StringReader(input));
		final State state = new State();

		List<Token> tokens = new ArrayList<>();
		while (charStream.hasMore()) {
			char c = charStream.charAtCurrentPosition();

			int line = charStream.getLine();
			int column = charStream.getColumn();

			if (c == '{' && charStream.peek(1) == '{') {
				tokens.add(new Token(Token.Type.VARIABLE_START, Syntax.VARIABLE_OPEN, line, column));
				charStream.skip(2);
				state.set(State.Type.VARIABLE);
			} else if (c == '{' && charStream.peek(1) == '%') {
				tokens.add(new Token(Token.Type.TAG_START, Syntax.TAG_OPEN, line, column));
				charStream.skip(2);
				state.set(State.Type.TAG);
				readTagContent(tokens, charStream); // Inhalte des Tags lesen
			} else if (c == '{' && charStream.peek(1) == '[') {
				tokens.add(new Token(Token.Type.COMPONENT_START, Syntax.COMPONENT_OPEN, line, column));
				charStream.skip(2);
				state.set(State.Type.COMPONENT);
				readComponentContent(tokens, charStream); 
			} else if (c == '{' && charStream.peek(1) == '#') {
				tokens.add(new Token(Token.Type.COMMENT_START, Syntax.COMMENT_OPEN, line, column));
				charStream.skip(2);
				state.set(State.Type.COMMENT);
			} else if (state.is(State.Type.TAG) && c == '%' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.TAG_END, Syntax.TAG_CLOSE, line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.COMPONENT) && c == ']' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.COMPONENT_END, Syntax.COMPONENT_CLOSE, line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE) && c == '}' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.VARIABLE_END, Syntax.VARIABLE_CLOSE, line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.COMMENT) && c == '#' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.COMMENT_END, Syntax.COMMENT_CLOSE, line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE)) {
				tokens.add(new Token(Token.Type.IDENTIFIER, readVariableContent(charStream), line, column));
			} else if (state.is(State.Type.COMMENT)) {
				tokens.add(new Token(Token.Type.COMMENT_VALUE, charStream.readUntil(Syntax.COMMENT_CLOSE), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else if (!state.is(State.Type.VARIABLE, State.Type.TAG)) {
				tokens.add(new Token(Token.Type.TEXT, charStream.readUntil(Syntax.OPENING), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else {
				charStream.advance();
			}
		}
		tokens.add(new Token(Token.Type.END, "", charStream.getLine(), charStream.getColumn()));
		return new TokenStream(tokens);
	}

	public static String readVariableContent(CharacterStream stream) {
		StringBuilder content = new StringBuilder();
		boolean insideQuotes = false;
		boolean escapeNext = false;

		while (stream.hasMore()) {
			char ch = stream.charAtCurrentPosition();

			if (escapeNext) {
				content.append(ch);
				escapeNext = false;
				stream.advance();
				continue;
			}

			if (ch == '\\') {
				escapeNext = true;
				stream.advance();
				continue;
			}

			if (ch == '"') {
				insideQuotes = !insideQuotes;
			}

			if (!insideQuotes && ch == '}' && stream.peek(1) == '}') {
				break;
			}

			content.append(ch);
			stream.advance();
		}

		return content.toString().trim();
	}
	
	private void readContent(List<Token> tokens, CharacterStream charStream, final String END) {
		charStream.skipWhitespace();

		String keyword = charStream.readWhile(Character::isLetterOrDigit);
		tokens.add(new Token(Token.Type.IDENTIFIER, keyword, charStream.getLine(), charStream.getColumn()));

		String condition = charStream.readUntil(END);
		tokens.add(new Token(Token.Type.EXPRESSION, condition, charStream.getLine(), charStream.getColumn()));
	}
	
	private void readTagContent(List<Token> tokens, CharacterStream charStream) {
		readContent(tokens, charStream, Syntax.TAG_CLOSE);
	}
	
	private void readComponentContent(List<Token> tokens, CharacterStream charStream) {
		readContent(tokens, charStream, Syntax.COMPONENT_CLOSE);
	}
}
