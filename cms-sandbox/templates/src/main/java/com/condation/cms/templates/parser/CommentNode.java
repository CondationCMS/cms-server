package com.condation.cms.templates.parser;

public class CommentNode extends ASTNode {

	private String value;
	
    public CommentNode() {
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	
    @Override
    public String toString() {
        return "CommentNode()";
    }
}
