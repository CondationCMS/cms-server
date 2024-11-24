package com.condation.cms.templates;

import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.parser.Parser;

public class TemplateEngine {

	private final TemplateConfiguration configuration;
	
	private Parser parser;

	public TemplateEngine(TemplateConfiguration configuration) {
		this.configuration = configuration;
		parser = new Parser(configuration);
	}
	
	public Template getTemplate (String template) {
		
		String templateString = configuration.getTemplateLoader().load(template);
		
		var tokenStream = new Lexer(templateString).tokenize();
		
		var rootNode = parser.parse(tokenStream);
		
		return new DefaultTemplate(rootNode);
	}
}
