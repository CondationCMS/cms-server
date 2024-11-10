package com.condation.cms.templates.parser;

public class VariableNode extends ASTNode {
    private String variable;

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return "VariableNode('" + variable + "')";
    }
}
