package com.condation.cms.templates.lexer;

public class Token {
    public static enum Type {
        TEXT, VARIABLE, TAG_START, TAG_END, IDENTIFIER, END, CONDITION
    }

    public final Type type;
    public final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}
