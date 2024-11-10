package com.condation.cms.templates.parser;

public class TagNode extends ASTNode {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TagNode('" + name + "')";
    }
}
