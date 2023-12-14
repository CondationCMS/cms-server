package com.github.thmarx.cms.modules.flexmark;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class FlexMarkMarkdownRenderer implements MarkdownRenderer {

	final MutableDataSet options = new MutableDataSet();
	private final Parser parser;
	private final HtmlRenderer renderer;
	
	public FlexMarkMarkdownRenderer () {
		options.set(Parser.EXTENSIONS, List.of(
				TablesExtension.create(),
				AnchorLinkExtension.create()
		));
		parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
	}
	
	@Override
	public String render (final String markdown) {
		Node document = parser.parse(markdown);
        return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
	}
	
	@Override
	public String excerpt (final String markdown, final int length) {
		Node document = parser.parse(markdown);
		TextCollectingVisitor textCollectingVisitor = new TextCollectingVisitor();
		String text = textCollectingVisitor.collectAndGetText(document);
		
		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}
}
