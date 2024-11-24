package com.condation.cms.templates.parser;

import com.condation.cms.templates.Tag;
import com.condation.cms.templates.TemplateConfiguration;
import java.util.List;
import java.util.Stack;

import com.condation.cms.templates.lexer.Token;
import static com.condation.cms.templates.lexer.Token.Type.VARIABLE_START;
import java.util.Optional;

public class Parser {

	private final TemplateConfiguration configuration;

//	private final List<Token> tokens;
//	private int position = 0;
	private final TokenStream tokenStream;

	public Parser(List<Token> tokens, TemplateConfiguration configuration) {
//		this.tokens = tokens;
		this.configuration = configuration;

		tokenStream = new TokenStream(tokens);
	}

	public ASTNode parse() {
		ASTNode root = new ASTNode();
		Stack<ASTNode> nodeStack = new Stack<>();
		nodeStack.push(root);

		Token token = null;
		while ((token = tokenStream.peek()) != null) {
			switch (token.type) {
				case TEXT: {
					nodeStack.peek().addChild(new TextNode(token.value));
					break;
				}
				case COMMENT_VALUE: {
					ASTNode node = nodeStack.peek();
					if (node instanceof CommentNode commentNode) {
						commentNode.setValue(token.value);
					}
					break;
				}
				case VARIABLE_START: {
					VariableNode variableNode = new VariableNode();
					nodeStack.peek().addChild(variableNode);
					nodeStack.push(variableNode); // In den neuen Kontext für Variablen wechseln
					break;
				}
				case COMMENT_START: {
					CommentNode commentNode = new CommentNode();
					nodeStack.peek().addChild(commentNode);
					nodeStack.push(commentNode); // In den neuen Kontext für Variablen wechseln
					break;
				}
				case TAG_START: {
					TagNode tagNode = new TagNode();

					nodeStack.peek().addChild(tagNode);
					nodeStack.push(tagNode); // In den neuen Kontext für Tags wechseln
					break;
				}
				case TAG_END: {
					if (!nodeStack.isEmpty() && nodeStack.peek() instanceof TagNode tempNode) {
						if (configuration.hasTag(tempNode.getName())) {
							Tag tag = configuration.getTag(tempNode.getName()).get();

							if (tag.isEndTag()) {
								nodeStack.pop();

								var temp = (TagNode) nodeStack.peek();

								var ptag = configuration.getTag(temp.getName()).get();

								if (ptag.getCloseTagName().isPresent()
										&& ptag.getCloseTagName().get().equals(tag.getTagName())) {
									nodeStack.pop();
								} else {
									throw new RuntimeException("invalid closing tag");
								}
							} else if (tag.getCloseTagName().isEmpty()) {
								nodeStack.pop();
							}

						} else {
							throw new RuntimeException("Undefined tag: " + tempNode.getName());
						}
					} else {
						throw new RuntimeException("Unexpected token: TAG_END");
					}
					break;
				}
				case VARIABLE_END: {
					if (!nodeStack.isEmpty()) {
						nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
					} else {
						throw new RuntimeException("Unexpected token: VARIABLE_END");
					}
					break;
				}
				case COMMENT_END: {
					if (!nodeStack.isEmpty()) {
						nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
					} else {
						throw new RuntimeException("Unexpected token: COMMENT_END");
					}
					break;
				}
				case IDENTIFIER: {
					ASTNode currentNode = nodeStack.peek();
					if (currentNode instanceof TagNode tagNode1) {
						tagNode1.setName(token.value); // Tag-Name setzen
					} else if (currentNode instanceof VariableNode variableNode1) {
						variableNode1.setVariable(token.value); // Variable setzen
					}
					break;
				}
				case EXPRESSION: {
					TagNode ifNode = (TagNode) nodeStack.peek();
					ifNode.setCondition(token.value);
					break;
				}
				case END: {
					System.out.println("end token?");
					break;
				}
				default:
					throw new RuntimeException("Unexpected token: " + token.type);
			}
			tokenStream.next();
		}

		if (nodeStack.size() > 1) {
			throw new RuntimeException("Unclosed tag or block detected");
		}

		return root;
	}
}
