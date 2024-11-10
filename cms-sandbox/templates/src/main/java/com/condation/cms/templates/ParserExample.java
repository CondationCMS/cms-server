package com.condation.cms.templates;

import java.util.List;

import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.Parser;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.TextNode;
import com.condation.cms.templates.parser.VariableNode;

public class ParserExample {
  public static void main(String[] args) {
        String template = "Hello {{ name }}! {% if condition %}World{% endif %}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.tokenize();
        
        Parser parser = new Parser(tokens);
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
            tag.getChildren().forEach(child -> printAST(child, depth + 1));
        }
    }
}
