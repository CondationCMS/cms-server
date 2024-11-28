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

	public Lexer() {
		
	}

	public TokenStream tokenize(String input) {

		CharacterStream charStream = new CharacterStream(input);
		final State state = new State();

		List<Token> tokens = new ArrayList<>();
		while (charStream.hasMore()) {
			char c = charStream.charAtCurrentPosition();

			int line = charStream.getLine();
			int column = charStream.getColumn();

			if (c == '{' && charStream.peek(1) == '{') {
				tokens.add(new Token(Token.Type.VARIABLE_START, "{{", line, column));
				charStream.skip(2);
				state.set(State.Type.VARIABLE);
			} else if (c == '{' && charStream.peek(1) == '%') {
				tokens.add(new Token(Token.Type.TAG_START, "{%", line, column));
				charStream.skip(2);
				state.set(State.Type.TAG);
				readTagContent(tokens, charStream); // Inhalte des Tags lesen
			} else if (c == '{' && charStream.peek(1) == '#') {
				tokens.add(new Token(Token.Type.COMMENT_START, "{*", line, column));
				charStream.skip(2);
				state.set(State.Type.COMMENT);
			} else if (state.is(State.Type.TAG) && c == '%' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.TAG_END, "%}", line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE) && c == '}' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.VARIABLE_END, "}}", line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.COMMENT) && c == '#' && charStream.peek(1) == '}') {
				tokens.add(new Token(Token.Type.COMMENT_END, "#}", line, column));
				charStream.skip(2);
				state.set(State.Type.NONE);
			} else if (state.is(State.Type.VARIABLE, State.Type.TAG) && Character.isLetterOrDigit(c)) {
				tokens.add(new Token(Token.Type.IDENTIFIER, charStream.readUntil("}}"), line, column));
			} else if (state.is(State.Type.COMMENT)) {
				tokens.add(new Token(Token.Type.COMMENT_VALUE, charStream.readUntil("#}"), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else if (!state.is(State.Type.VARIABLE, State.Type.TAG)) {
				tokens.add(new Token(Token.Type.TEXT, charStream.readUntil("{"), line, column)); // Alles bis zum nächsten '{' als Text speichern
			} else {
				charStream.advance();
			}
		}
		tokens.add(new Token(Token.Type.END, "", charStream.getLine(), charStream.getColumn()));
		return new TokenStream(tokens);
	}

	private void readTagContent(List<Token> tokens, CharacterStream charStream) {
		charStream.skipWhitespace();

		String keyword = charStream.readWhile(Character::isLetter);
		tokens.add(new Token(Token.Type.IDENTIFIER, keyword, charStream.getLine(), charStream.getColumn()));

		String condition = charStream.readUntil("%}");
		tokens.add(new Token(Token.Type.EXPRESSION, condition, charStream.getLine(), charStream.getColumn()));
	}
}
