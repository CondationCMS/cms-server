package com.condation.cms.templates.parser;

import com.condation.cms.templates.lexer.Token;
import java.util.List;

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
