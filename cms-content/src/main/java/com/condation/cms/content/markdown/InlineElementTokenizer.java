package com.condation.cms.content.markdown;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.content.markdown.rules.inline.TextInlineRule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Inline-level markdown tokenizer with recursion depth limit.
 * Returns {@link LocatedInlineBlock} instances whose absolute positions are
 * correct offsets into the original document string.
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class InlineElementTokenizer {

	private final Options options;
	private static final int MAX_RECURSION_DEPTH = 100;

	/**
	 * Tokenizes inline markdown without document-offset tracking (legacy entry point).
	 * Absolute positions will equal relative positions (documentOffset = 0).
	 */
	public List<LocatedInlineBlock> tokenize(final String original_md) throws IOException {
		return tokenize(original_md, 0);
	}

	/**
	 * Tokenizes inline markdown with a known document offset.
	 *
	 * @param original_md    inline markdown content (block body)
	 * @param documentOffset absolute start of this content in the full document
	 */
	public List<LocatedInlineBlock> tokenize(final String original_md, int documentOffset) throws IOException {
		return doTokenize(this, original_md, documentOffset, 0);
	}

	protected List<LocatedInlineBlock> doTokenize(
			final InlineElementTokenizer tokenizer,
			final String original_md,
			int documentOffset,
			int depth) throws IOException {

		if (depth > MAX_RECURSION_DEPTH) {
			throw new IOException("Maximum recursion depth exceeded in inline parsing");
		}

		var md = original_md.replaceAll("\r\n", "\n");
		StringBuilder mdBuilder = new StringBuilder(md);
		int offset = documentOffset;

		final List<LocatedInlineBlock> blocks = new ArrayList<>();

		for (var blockRule : options.inlineElementRules) {
			InlineBlock block = null;
			while ((block = blockRule.next(tokenizer, mdBuilder.toString())) != null) {

				if (block.start() != 0) {
					var before = mdBuilder.substring(0, block.start());
					blocks.addAll(doTokenize(tokenizer, before, offset, depth + 1));
					offset += block.start();
				}

				blocks.add(new LocatedInlineBlock(block, offset, offset + (block.end() - block.start())));
				offset += block.end() - block.start();
				mdBuilder.delete(0, block.end());
			}
		}

		if (mdBuilder.length() > 0) {
			blocks.add(new LocatedInlineBlock(
					new TextInlineRule.TextBlock(0, mdBuilder.length(), mdBuilder.toString()),
					offset,
					offset + mdBuilder.length()));
		}

		return blocks;
	}
}
