package com.condation.cms.templates.tags;

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
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class IncludeTag implements Tag {

	@Override
	public String getTagName() {
		return "include";
	}

	@Override
	public void render(TagNode node, Renderer.Context context, StringBuilder sb) {
		try {
			var templateString = getTemplate(node, context);
			
			var template = (DefaultTemplate)context.templateEngine().getTemplate(templateString);
			if (template != null) {
				String result = template.evaluate(context.scopes());
				sb.append(result);
			}
		} catch (Exception e) {
			throw new TagException("error including template", node.getLine(), node.getColumn());
		}
	}
	
	private String getTemplate (TagNode node, Renderer.Context context) {
		var template = node.getCondition().trim();
		
		var scope = context.createEngineContext();
		return (String)context.engine().createExpression(template).evaluate(scope);
	}
}
