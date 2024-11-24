package com.condation.cms.templates;

import java.util.List;

import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.Parser;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.TextNode;
import com.condation.cms.templates.parser.VariableNode;
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.IfTag;

public class ParserExample {

	static TemplateConfiguration config = new TemplateConfiguration();

	static {
		config.registerTag(new IfTag());
		config.registerTag(new ElseIfTag());
		config.registerTag(new ElseTag());
		config.registerTag(new EndIfTag());
	}

	public static void main(String[] args) {

		System.out.println("TEST 1");
		run_example(
				"Hello {{ name }}! {% if condition %}World{% endif %}"
		);

		System.out.println("TEST 2");
		run_example(
				"""
                Hello {{ name }}! 
                {% if condition %}
                    Hello
                {% elseif condition2 %}
					World
                {% else %}
                   Moon
                {% endif %}
                Bye {{ name }}
            """
		);

		System.out.println("TEST 3");
		run_example(
				"""
                {% if condition %}
                    World
                {% elseif condition2 %}
            {#
				Here is a comment
            #}
                {% endif %}
            """
		);
	}

	public static void run_example(String template) {
		Lexer lexer = new Lexer(template);
		List<Token> tokens = lexer.tokenize();

		Parser parser = new Parser(tokens, config);
		ASTNode ast = parser.parse();

		System.out.println("Tokens:");
		tokens.forEach(System.out::println);

		System.out.println("AST:");
		printAST(ast, 0);
	}

	private static void printAST(ASTNode node, int depth) {
		String indent = " ".repeat(depth * 2);
		if (node instanceof TextNode) {
			System.out.println(indent + "Text: " + ((TextNode) node).text);
		} else if (node instanceof VariableNode) {
			System.out.println(indent + "Variable: " + ((VariableNode) node).getVariable());
		} else if (node instanceof TagNode) {
			TagNode tag = (TagNode) node;
			System.out.println(indent + "Tag: " + tag.getName());
			System.out.println(indent + "Condition: " + tag.getCondition());
			tag.getChildren().forEach(child -> printAST(child, depth + 1));
		} else if (!node.getChildren().isEmpty()) {
			node.getChildren().forEach(child -> printAST(child, depth + 1));
		}
	}
}
