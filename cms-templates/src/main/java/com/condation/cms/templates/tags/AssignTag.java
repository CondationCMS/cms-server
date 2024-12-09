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
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import java.io.Writer;

/**
 *
 * @author t.marx
 */
public class AssignTag extends AbstractTag implements Tag {

	@Override
	public String getTagName() {
		return "assign";
	}

	@Override
	public boolean parseExpressions() {
		return true;
	}

	@Override
	public void render(TagNode node, Renderer.Context context, Writer writer) {
		var scopeContext = context.createEngineContext();
		
//		node.getExpression().evaluate(scopeContext);
		evaluateExpression(node, node.getExpression(), context, scopeContext);
	}
}
