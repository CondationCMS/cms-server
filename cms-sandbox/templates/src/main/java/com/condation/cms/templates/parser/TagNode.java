package com.condation.cms.templates.parser;

import java.util.ArrayList;
import java.util.List;

public class TagNode extends ASTNode {
    private String name;
    private String condition;
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCondition (String condition) {
        this.condition = condition;
    }

    public String getCondition () {
        return condition;
    }

    @Override
    public String toString() {
        return "TagNode('" + name + ", " + condition + "')";
    }
}
