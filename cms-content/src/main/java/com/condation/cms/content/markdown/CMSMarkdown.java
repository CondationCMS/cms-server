package com.condation.cms.content.markdown;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;
import com.condation.cms.content.markdown.rules.inline.TextInlineRule;
import com.condation.cms.content.markdown.utils.StringUtils;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class CMSMarkdown {

	private final BlockTokenizer blockTokenizer;

	private final InlineElementTokenizer inlineTokenizer;

	private final List<BlockElementRule> blockRules;
	private final List<InlineElementRule> inlineRules;

	public CMSMarkdown(Options options) {
		this.blockTokenizer = new BlockTokenizer(options);
		this.inlineTokenizer = new InlineElementTokenizer(options);
		blockRules = options.blockElementRules;
		blockRules.addLast(new ParagraphBlockRule());
		inlineRules = options.inlineElementRules;
		inlineRules.addLast(new TextInlineRule());
	}

	private String renderInlineElements(final String inline_md) throws IOException {
		final StringBuilder htmlBuilder = new StringBuilder();
		List<InlineBlock> blocks = inlineTokenizer.tokenize(inline_md);

		blocks.stream()
				.map(block -> block.render())
				.forEach(blockHtml -> {
					htmlBuilder.append(blockHtml);
				});

		return htmlBuilder.toString();
	}

	public String render(final String md) throws IOException {
		final StringBuilder htmlBuilder = new StringBuilder();
		List<Block> blocks = blockTokenizer.tokenize(
				StringUtils.escape(md)
		);

		InlineRenderer inlineRenderer = (content) -> {
			try {
				return renderInlineElements(content);
			} catch (IOException ioe) {
			}
			return "";
		};
		BlockRenderer blockRenderer = (content) -> {
			try {
				return this.render(content);
			} catch (IOException e) {
			}
			return "";
		};

		blocks.stream()
				.map(block -> {
					if (block instanceof BlockContainer) {
						return ((BlockContainer) block).render(blockRenderer);
					} else {
						return block.render(inlineRenderer);
					}
				})
				.forEach(blockHtml -> {
					htmlBuilder.append(blockHtml);
				});

		return StringUtils.unescape(htmlBuilder.toString());
	}
}
