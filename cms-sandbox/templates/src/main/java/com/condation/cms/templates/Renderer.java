package com.condation.cms.templates;

import java.util.HashMap;
import java.util.Map;

import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.TextNode;
import com.condation.cms.templates.parser.VariableNode;

public class Renderer {

    public static class Context {
        private final Map<String, String> variables = new HashMap<>();

        public void setVariable(String name, String value) {
            variables.put(name, value);
        }

        public String getVariable(String name) {
            return variables.get(name);
        }
    }

    private final Context context;

    public Renderer(Context context) {
        this.context = context;
    }

    public String render(ASTNode node) {
        StringBuilder output = new StringBuilder();
        renderNode(node, output);
        return output.toString();
    }

    private void renderNode(ASTNode node, StringBuilder output) {
        if (node instanceof TextNode) {
            output.append(((TextNode) node).text);
        } else if (node instanceof VariableNode) {
            String variableValue = context.getVariable(((VariableNode) node).getVariable());
            output.append(variableValue != null ? variableValue : "");
        } else if (node instanceof TagNode) {
            TagNode tagNode = (TagNode) node;
            // Beispiel: Wir unterstützen das "if"-Tag
            if ("if".equals(tagNode.getName())) {
                ASTNode conditionNode = tagNode.getChildren().get(0);
                if (conditionNode instanceof VariableNode) {
                    String variableValue = context.getVariable(((VariableNode) conditionNode).getVariable());
                    if (variableValue != null && !variableValue.isEmpty()) {
                        for (int i = 1; i < tagNode.getChildren().size(); i++) {
                            renderNode(tagNode.getChildren().get(i), output);
                        }
                    }
                }
            } else {
                // Anderes Tag-Verhalten kann hier hinzugefügt werden
                for (ASTNode child : tagNode.getChildren()) {
                    renderNode(child, output);
                }
            }
        } else {
            // Rekursiv alle Kindknoten verarbeiten
            for (ASTNode child : node.getChildren()) {
                renderNode(child, output);
            }
        }
    }
}
