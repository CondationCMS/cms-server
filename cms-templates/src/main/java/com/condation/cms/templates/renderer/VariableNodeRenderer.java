package com.condation.cms.templates.renderer;

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
import com.condation.cms.templates.parser.Filter;
import com.condation.cms.templates.parser.VariableNode;
import com.condation.cms.templates.renderer.Renderer.Context;
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.filter.FilterPipeline;
import java.io.Writer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
class VariableNodeRenderer {

	private final TemplateConfiguration templateConfiguration;

	protected void render(VariableNode node, Context context, Writer writer) {
		try {
			Object variableValue = node.getExpression().evaluate(context.createEngineContext());
			if (variableValue != null && variableValue instanceof String stringValue) {
				writer.append(evaluateStringFilters(stringValue, node.getFilters(), context));
			} else if (variableValue != null)  {
				writer.append(evaluateFilters(variableValue, node.getFilters(), context));
			}
		} catch (Exception e) {
			throw new RenderException(e.getLocalizedMessage(), node.getLine(), node.getColumn());
		}
	}

	protected String evaluateFilters(Object value, List<Filter> filters, Context context) {
		var returnValue = value;
		if (filters != null && !filters.isEmpty()) {
			var filterPipeline = createPipeline(filters, context);

			returnValue = filterPipeline.execute(returnValue);
		}

		return String.valueOf(returnValue);
	}

	protected String evaluateStringFilters(String value, List<Filter> filters, Context context) {

		var returnValue = StringEscapeUtils.ESCAPE_HTML4.translate(value);

		if (filters != null && !filters.isEmpty()) {
			var filterPipeline = createPipeline(filters, context);

			returnValue = (String) filterPipeline.execute(returnValue);
		}

		return returnValue;
	}

	private FilterPipeline createPipeline(List<Filter> filters, Context context) {
		var filterPipeline = new FilterPipeline(templateConfiguration.getFilterRegistry());

		var engineScope = context.createEngineContext();
		for (Filter filter : filters) {
			var params = filter.parameters()
					.stream()
					.map(param -> {
						var exp = context.engine().createExpression(param);
						return exp.evaluate(engineScope);
					}).toArray();
			filterPipeline.addStep(filter.name(), params);
		}

		return filterPipeline;
	}
}
