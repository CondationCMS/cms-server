package com.condation.cms.templates;

import java.util.List;
import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.Parser;

public class RenderTemplateExample {
    public static void main(String[] args) {
		
		TemplateConfiguration config = new TemplateConfiguration();
		
        // Beispiel-Template und Kontext
        String template = "Hello, {% if user %}{{ user }}{% endif %}!";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens, config);
        ASTNode ast = parser.parse();

        // Kontext mit Werten für das Template
        var context = new Renderer.Context();
        context.setVariable("user", "Thorsten");

        // Rendering
        Renderer renderer = new Renderer(context);
        String output = renderer.render(ast);

        System.out.println("Rendered Output:");
        System.out.println(output);
    }
}
