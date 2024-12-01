package com.condation.cms.templates;

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

import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.parser.Parser;
import com.condation.cms.templates.renderer.Renderer;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;

public class TemplateEngine {

	private final JexlEngine jexl = new JexlBuilder()
			.cache(512)
			.strict(true)
			.silent(false)
			.create();
	
	private final TemplateConfiguration configuration;
	
	private final Parser parser;
	private final Lexer lexer;
	private final Renderer renderer;
	
	private final TemplateCache templateCache;

	public TemplateEngine(TemplateConfiguration configuration) {
		this.configuration = configuration;
		this.templateCache = configuration.getTemplateCache();
		this.parser = new Parser(configuration, jexl);
		this.lexer = new Lexer();
		this.renderer = new Renderer(configuration, this, jexl);
	}
	
	public void invalidateTemplateCache () {
		
	}
	
	public Template getTemplate (String template) {
		
		if (templateCache != null && templateCache.contains(template)) {
			return templateCache.get(template).get();
		}
		
		String templateString = configuration.getTemplateLoader().load(template);
		var tokenStream = lexer.tokenize(templateString);
		var rootNode = parser.parse(tokenStream);
		var temp = new DefaultTemplate(rootNode, renderer);
		
		if (templateCache != null) {
			templateCache.put(template, temp);
		}
		
		return temp;
	}
}
