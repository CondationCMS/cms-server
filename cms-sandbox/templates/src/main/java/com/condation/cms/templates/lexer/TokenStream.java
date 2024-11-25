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

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author t.marx
 */
public class TokenStream {

	private final List<Token> tokens;

	private int position = 0;

	public TokenStream(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	public void forEach (Consumer<Token> tokenConsumer) {
		tokens.forEach(tokenConsumer);
	}

	public Token next() {
		if (position >= tokens.size()) {
			return null;
		}
		return tokens.get(position++);
	}

	public Token peek() {
		if (position >= tokens.size()) {
			return null;
		}
		return tokens.get(position);
	}

	public void skip() {
		skip(1);
	}

	public void skip(int n) {
		position += n;
	}

	/**
	 * Setzt die Position auf einen bestimmten Index zurück.
	 *
	 * @param position Die neue Position (muss innerhalb der Grenzen liegen).
	 * @throws IllegalArgumentException Wenn die Position ungültig ist.
	 */
	public void reset(int position) {
		if (position < 0 || position > tokens.size()) {
			throw new IllegalArgumentException("Invalid position: " + position);
		}
		this.position = position;
	}

	/**
	 * Gibt die aktuelle Position im Stream zurück.
	 *
	 * @return Die aktuelle Position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Gibt an, ob das Ende des Streams erreicht wurde.
	 *
	 * @return true, wenn keine weiteren Tokens verfügbar sind.
	 */
	public boolean isEnd() {
		return position >= tokens.size();
	}
}
