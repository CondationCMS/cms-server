package com.condation.cms.templates.parser;

public class TextNode extends ASTNode {
    public final String text;

    public TextNode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextNode('" + text + "')";
    }
}
