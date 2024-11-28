package com.condation.cms.templates.lexer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CharacterStream {

    private final String source;

    private int position = 0;

    @Getter
	private int line = 1;
    @Getter
	private int column = 1;


    public boolean hasMore () {
        return position < source.length();
    }

    public char charAtCurrentPosition () {
        return source.charAt(position);
    }

    protected char peek(int offset) {
		return (position + offset < source.length()) ? source.charAt(position + offset) : '\0';
	}

	protected String readWhile(java.util.function.Predicate<Character> condition) {
		StringBuilder result = new StringBuilder();
		while (position < source.length() && condition.test(source.charAt(position))) {
			result.append(source.charAt(position));
			
			advance();
		}
		return result.toString();
	}

	protected String readUntil(String delimiter) {
		StringBuilder result = new StringBuilder();
		while (position < source.length() && !source.startsWith(delimiter, position)) {
			result.append(source.charAt(position));
			advance();
		}
		return result.toString();
	}

	protected void skipWhitespace() {
		while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
			advance();
		}
	}

    protected void skip (int count) {
        position += count;
    }

	protected void advance() {
		if (source.charAt(position) == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}
		position++;
	}

}
