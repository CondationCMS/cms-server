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

import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import com.condation.cms.templates.expression.CMSPermissions;
import com.condation.cms.templates.expression.RecordResolverStrategy;
import com.condation.cms.templates.lexer.Lexer;
import com.condation.cms.templates.parser.Parser;
import com.condation.cms.templates.renderer.Renderer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;

@Slf4j
public class CMSTemplateEngine {

	private final JexlEngine jexl;
	
	private final TemplateConfiguration configuration;
	
	private final Parser parser;
	private final Lexer lexer;
	private final Renderer renderer;
	
	private final TemplateCache templateCache;

	public CMSTemplateEngine(TemplateConfiguration configuration) {
		this.configuration = configuration;
		jexl = createJexlEngine();
		this.templateCache = configuration.getTemplateCache();
		this.parser = new Parser(configuration, jexl);
		this.lexer = new Lexer();
		this.renderer = new Renderer(configuration, this, jexl);
	}
	
	private JexlEngine createJexlEngine() {
        JexlBuilder builder = new JexlBuilder();

		if (configuration.isDevMode()) {
			builder
					.cache(512)
					.safe(false)
					.strict(true)
					.silent(false)
					.debug(false);
		} else {
			builder
					.cache(1024)
					.safe(true)
					.strict(false)
					.silent(true)
					.debug(false);
		}

		builder.strategy(new RecordResolverStrategy());
		
        return builder.permissions(CMSPermissions.PERMISSIONS).create();
    }
	
	
	public void invalidateTemplateCache () {
		templateCache.invalidate();
	}
	
	public Template getTemplateFromString (String templateContent) {
		var tokenStream = lexer.tokenize(templateContent);
		var rootNode = parser.parse(tokenStream);
		return new DefaultTemplate(rootNode, renderer);
	}
	
	private boolean useCache () {
		final boolean useTemplateCache = !configuration.isDevMode() && templateCache != null;
		return useTemplateCache;
	}
	
	public Template getTemplate (String template) {
		
		if (useCache() && templateCache.contains(template)) {
			return templateCache.get(template).get();
		}
		
		String templateString = configuration.getTemplateLoader().load(template);
		if (templateString == null) {
			throw new TemplateNotFoundException("template %s not found".formatted(template));
		}
		var tokenStream = lexer.tokenize(templateString);
		var rootNode = parser.parse(tokenStream);
		var temp = new DefaultTemplate(rootNode, renderer);
		
		if (useCache()) {
			templateCache.put(template, temp);
		}
		
		return temp;
	}
}
