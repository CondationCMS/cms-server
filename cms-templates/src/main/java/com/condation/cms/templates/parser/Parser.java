package com.condation.cms.templates.parser;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.condation.cms.templates.lexer.TokenStream;
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.exceptions.UnknownTagException;
import com.condation.cms.templates.utils.TemplateUtils;
import java.util.Stack;

import com.condation.cms.templates.lexer.Token;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;

@RequiredArgsConstructor
public class Parser {

	private final TemplateConfiguration configuration;

	private final JexlEngine engine;

	public ASTNode parse(final TokenStream tokenStream) {
		ASTNode root = new ASTNode(0, 0);
		Stack<ASTNode> nodeStack = new Stack<>();
		nodeStack.push(root);

		Token token = null;
		while ((token = tokenStream.peek()) != null) {
			switch (token.type) {
				case TEXT: {
					nodeStack.peek().addChild(new TextNode(token.value, token.line, token.column));
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
					VariableNode variableNode = new VariableNode(token.line, token.column);
					nodeStack.peek().addChild(variableNode);
					nodeStack.push(variableNode); // In den neuen Kontext für Variablen wechseln
					break;
				}
				case COMMENT_START: {
					CommentNode commentNode = new CommentNode(token.line, token.column);
					nodeStack.peek().addChild(commentNode);
					nodeStack.push(commentNode); // In den neuen Kontext für Variablen wechseln
					break;
				}
				case TAG_START: {
					TagNode tagNode = new TagNode(token.line, token.column);

					nodeStack.peek().addChild(tagNode);
					nodeStack.push(tagNode); // In den neuen Kontext für Tags wechseln
					break;
				}
				case TAG_END: {
					if (!nodeStack.isEmpty() && nodeStack.peek() instanceof TagNode tempNode) {
						if (configuration.hasTag(tempNode.getName())) {
							Tag tag = configuration.getTag(tempNode.getName()).get();

							if (tag.isClosingTag()) {
								nodeStack.pop();

								var temp = (TagNode) nodeStack.peek();

								var ptag = configuration.getTag(temp.getName()).get();

								if (ptag.getCloseTagName().isPresent()
										&& ptag.getCloseTagName().get().equals(tag.getTagName())) {
									nodeStack.pop();
								} else {
									throw new ParserException("invalid closing tag", token.line, token.column);
								}
							} else if (tag.getCloseTagName().isEmpty()) {
								nodeStack.pop();
							}

						} else {
							throw new ParserException("Undefined tag: " + tempNode.getName(), token.line, token.column);
						}
					} else {
						throw new ParserException("Unexpected token: TAG_END", token.line, token.column);
					}
					break;
				}
				case VARIABLE_END: {
					if (!nodeStack.isEmpty()) {
						nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
					} else {
						throw new ParserException("Unexpected token: VARIABLE_END", token.line, token.column);
					}
					break;
				}
				case COMMENT_END: {
					if (!nodeStack.isEmpty()) {
						nodeStack.pop(); // Aus dem aktuellen Tag-/Variable-Block heraustreten
					} else {
						throw new ParserException("Unexpected token: COMMENT_END", token.line, token.column);
					}
					break;
				}
				case IDENTIFIER: {
					ASTNode currentNode = nodeStack.peek();
					if (currentNode instanceof TagNode tagNode1) {
						tagNode1.setName(token.value); // Tag-Name setzen
					} else if (currentNode instanceof VariableNode variableNode1) {
						var identifier = token.value;
						if (TemplateUtils.hasFilters(identifier)) {
							var variable = TemplateUtils.extractVariableName(identifier);
							
							variableNode1.setVariable(variable); // Variable setzen
							variableNode1.setExpression(engine.createExpression(variable));
							
							variableNode1.setFilters(TemplateUtils.extractFilters(identifier)
									.stream()
									.map(TemplateUtils::parseFilter)
									.toList()
							);
						} else {
							variableNode1.setVariable(token.value); // Variable setzen
							variableNode1.setExpression(engine.createExpression(token.value));
						}
					}
					break;
				}
				case EXPRESSION: {
					ASTNode currentNode = nodeStack.peek();
					if (currentNode instanceof TagNode tagNode) {
						tagNode.setCondition(token.value);
						
						if (configuration.getTag(tagNode.getName()).isEmpty()) {
							throw new UnknownTagException("unkown tag (%s)".formatted(tagNode.getName()), currentNode.getLine(), currentNode.getColumn());
						}
						
						Tag tag = configuration.getTag(tagNode.getName()).get();
						if (tag.parseExpressions()) {
							tagNode.setExpression(engine.createExpression(token.value));
						}
					} else if (currentNode instanceof TagNode vNode) {
						
					}
					
					break;
				}
				case END: {
					break;
				}
				default:
					throw new ParserException("Unexpected token: " + token.type, token.line, token.column);
			}
			tokenStream.next();
		}

		if (nodeStack.size() > 1) {
			throw new ParserException("Unclosed tag or block detected", 0, 0);
		}

		return root;
	}
}
