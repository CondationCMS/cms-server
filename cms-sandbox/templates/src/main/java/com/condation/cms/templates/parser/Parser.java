package com.condation.cms.templates.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.condation.cms.templates.lexer.Token;
import static com.condation.cms.templates.lexer.Token.Type.VARIABLE_START;

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
				case COMMENT_VALUE:
					ASTNode node = nodeStack.peek();
                    if (node instanceof CommentNode commentNode) {
						commentNode.setValue(token.value);
					}
					break;
                case VARIABLE_START:
                    VariableNode variableNode = new VariableNode();
                    nodeStack.peek().addChild(variableNode);
                    nodeStack.push(variableNode); // In den neuen Kontext für Variablen wechseln
                    break;
				case COMMENT_START:
                    CommentNode commentNode = new CommentNode();
                    nodeStack.peek().addChild(commentNode);
                    nodeStack.push(commentNode); // In den neuen Kontext für Variablen wechseln
                    break;
                case TAG_START:
                    TagNode tagNode = new TagNode();
                    
                    nodeStack.peek().addChild(tagNode);
                    nodeStack.push(tagNode); // In den neuen Kontext für Tags wechseln
                    break;
                case TAG_END:
				case VARIABLE_END:
				case COMMENT_END:
                    if (!nodeStack.isEmpty()) {
                        nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
                    } else {
                        throw new RuntimeException("Unexpected token: TAG_END");
                    }
                    break;
                case IDENTIFIER:
                    ASTNode currentNode = nodeStack.peek();
                    if (currentNode instanceof TagNode tagNode1) {
                        tagNode1.setName(token.value); // Tag-Name setzen
                    } else if (currentNode instanceof VariableNode variableNode1) {
                        variableNode1.setVariable(token.value); // Variable setzen
                    }
                    break;

                case EXPRESSION:
                    TagNode ifNode = (TagNode) nodeStack.peek();
                    ifNode.setCondition(token.value);
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
