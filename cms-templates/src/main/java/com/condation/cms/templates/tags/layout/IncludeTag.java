package com.condation.cms.templates.tags.layout;

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

import com.condation.cms.templates.DefaultTemplate;
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.exceptions.TagException;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.tags.AbstractTag;
import java.io.StringWriter;
import java.io.Writer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlExpression;

/**
 *
 * @author t.marx
 */
@Slf4j
public class IncludeTag extends AbstractTag implements Tag {

	@Override
	public String getTagName() {
		return "include";
	}

	@Override
	public void render(TagNode node, Renderer.Context context, Writer writer) {
		try {
			var templateString = getTemplate(node, context);
			
			var template = (DefaultTemplate)context.templateEngine().getTemplate(templateString);
			if (template != null) {
				StringWriter childWriter = new StringWriter();
				template.evaluate(context.scopes(), childWriter);
				writer.write(childWriter.toString());
			}
		} catch (Exception e) {
			throw new TagException("error including template", node.getLine(), node.getColumn());
		}
	}
	
	private String getTemplate (TagNode node, Renderer.Context context) {
		var template = node.getCondition().trim();
		
		var scope = context.createEngineContext();
		final JexlExpression expression = context.engine().createExpression(template);
		return (String)evaluateExpression(node, expression, context, scope);
	}
}
