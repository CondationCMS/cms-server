package com.condation.cms.templates.lexer;

public class Token {
    public static enum Type {
        TEXT, 
		VARIABLE_START,
		VARIABLE_END,
		TAG_START, 
		TAG_END, 
		COMMENT_START,
		COMMENT_END,
		COMMENT_VALUE,
		IDENTIFIER, 
		END, 
		EXPRESSION
    }

    public final Type type;
    public final String value;
	public final int line;
    public final int column;

    public Token(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
		this.line = line;
		this.column = column;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}
