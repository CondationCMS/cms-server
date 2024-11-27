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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
class VariableNodeRenderer {

	protected void render(VariableNode node, ScopeContext context, StringBuilder output) {
		Object variableValue = node.getExpression().evaluate(context);
		if (variableValue != null && variableValue instanceof String stringValue) {
			output.append(evaluateStringFilters(stringValue, node.getFilters()));
		} else {
			output.append(variableValue != null ? variableValue : "");
		}
	}

	protected String evaluateStringFilters(String value, List<Filter> filters) {

		var returnValue = StringEscapeUtils.ESCAPE_HTML4.translate(value);

		if (filters != null && !filters.isEmpty()) {
			for (var filter : filters) {
				returnValue = switch (filter.name()) {
					case "raw" ->
						StringEscapeUtils.UNESCAPE_HTML4.translate(value);
					case "trim" ->
						returnValue.trim();
					default ->
						returnValue;
				};
			}
		}

		return returnValue;
	}
}
