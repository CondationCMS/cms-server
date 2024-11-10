package com.condation.cms.templates.parser;

import java.util.List;
import java.util.Stack;

import com.condation.cms.templates.lexer.Token;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        ASTNode root = new ASTNode();
        Stack<ASTNode> nodeStack = new Stack<>();
        nodeStack.push(root);

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            switch (token.type) {
                case TEXT:
                    nodeStack.peek().addChild(new TextNode(token.value));
                    break;
                case VARIABLE:
                    VariableNode variableNode = new VariableNode();
                    nodeStack.peek().addChild(variableNode);
                    nodeStack.push(variableNode); // In den neuen Kontext für Variablen wechseln
                    break;
                case TAG_START:
                    TagNode tagNode = new TagNode();
                    nodeStack.peek().addChild(tagNode);
                    nodeStack.push(tagNode); // In den neuen Kontext für Tags wechseln
                    break;
                case TAG_END:
                    if (!nodeStack.isEmpty()) {
                        nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
                    }
                    break;
                case IDENTIFIER:
                    ASTNode currentNode = nodeStack.peek();
                    if (currentNode instanceof TagNode) {
                        ((TagNode) currentNode).setName(token.value); // Tag-Name setzen
                    } else if (currentNode instanceof VariableNode) {
                        ((VariableNode) currentNode).setVariable(token.value); // Variable setzen
                    }
                    break;
                case END:
                    System.out.println("end token?");
                    break;
                default:
                    throw new RuntimeException("Unexpected token: " + token.type);
            }
            position++;
        }

        return root;
    }
}
