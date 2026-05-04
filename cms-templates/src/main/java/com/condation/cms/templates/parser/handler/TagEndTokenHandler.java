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

import com.condation.cms.templates.Tag;
import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ParserContext;
import com.condation.cms.templates.parser.TagNode;

/**
 * Handles TAG_END tokens by validating and closing tag blocks.
 * Handles both self-closing tags and tags with closing counterparts.
 */
public class TagEndTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (!context.hasNodes() || !(context.currentNode() instanceof TagNode currentTagNode)) {
			throw new ParserException("Unexpected token: TAG_END", token.line, token.column);
		}

		if (!context.getConfiguration().hasTag(currentTagNode.getName())) {
			throw new ParserException("Undefined tag: " + currentTagNode.getName(), token.line, token.column);
		}

		Tag tag = context.getConfiguration().getTag(currentTagNode.getName()).get();

		if (tag.isClosingTag()) {
			handleClosingTag(token, context, currentTagNode, tag);
		} else if (tag.getCloseTagNames() == null || tag.getCloseTagNames().isEmpty()) {
			// Self-closing tag or tag without close counterpart
			context.popNode();
		}
		// Tags with close counterpart remain on stack until their closing tag appears
	}

	private void handleClosingTag(Token token, ParserContext context, TagNode closingTagNode, Tag closingTag) {
		context.popNode(); // Pop the closing tag itself

		if (!context.hasNodes() || !(context.currentNode() instanceof TagNode openingTagNode)) {
			throw new ParserException("Invalid closing tag", token.line, token.column);
		}

		Tag openingTag = context.getConfiguration().getTag(openingTagNode.getName()).get();

		if (openingTag.getCloseTagNames() != null
				&& openingTag.getCloseTagNames().contains(closingTag.getTagName())) {
			context.popNode(); // Pop the opening tag
		} else {
			throw new ParserException("Invalid closing tag", token.line, token.column);
		}
	}
}
