package com.condation.cms.templates.parser;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private final List<ASTNode> children = new ArrayList<>();

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public List<ASTNode> getChildren() {
        return children;
    }
}
