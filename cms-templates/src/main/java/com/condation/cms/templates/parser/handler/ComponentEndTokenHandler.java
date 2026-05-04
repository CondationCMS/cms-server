package com.condation.cms.templates.parser.handler;

/*-
 * #%L
 * CMS Templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.parser.ParserContext;
import com.google.common.base.Strings;

/**
 * Handles COMPONENT_END tokens by validating and closing component blocks.
 * Components can have closing counterparts (e.g., {[mycomp]} ... {[endmycomp]}).
 */
public class ComponentEndTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (!context.hasNodes() || !(context.currentNode() instanceof ComponentNode currentComponent)) {
			throw new ParserException("Unexpected token: COMPONENT_END", token.line, token.column);
		}

		String componentName = currentComponent.getName();
        if (Strings.isNullOrEmpty(componentName)) {
            // should only occure on template errors
            return;
        }
		boolean isClosingComponent = componentName.startsWith("end") || componentName.startsWith("/");

		if (isClosingComponent) {
			String expectedOpeningName = componentName.replaceFirst("^(end|/)", "");
			handleClosingComponent(token, context, expectedOpeningName);
		}
	}

	private void handleClosingComponent(Token token, ParserContext context, String expectedOpeningName) {
		context.popNode(); // Pop the closing component

		if (!context.hasNodes() || !(context.currentNode() instanceof ComponentNode openingComponent)) {
			throw new ParserException("Invalid closing component", token.line, token.column);
		}

		if (expectedOpeningName.equals(openingComponent.getName())) {
			context.popNode(); // Pop the opening component
		} else {
			throw new ParserException("Invalid closing component", token.line, token.column);
		}
	}
}
