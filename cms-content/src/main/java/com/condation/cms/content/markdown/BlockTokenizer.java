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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Block-level markdown tokenizer with recursion depth limit.
 * Returns {@link LocatedBlock} instances whose {@code absoluteStart}/
 * {@code absoluteEnd} are correct offsets into the original document string.
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class BlockTokenizer {

	private final Options options;
	private static final int MAX_RECURSION_DEPTH = 100;

	protected List<LocatedBlock> tokenize(final String original_md) throws IOException {
		return tokenizeWithDepth(original_md, 0, 0);
	}

	/**
	 * @param original_md  the markdown substring to tokenize
	 * @param documentOffset  cumulative character offset of this substring in the full document
	 * @param depth  current recursion depth
	 */
	private List<LocatedBlock> tokenizeWithDepth(final String original_md, int documentOffset, int depth) throws IOException {
		if (depth > MAX_RECURSION_DEPTH) {
			throw new IOException("Maximum recursion depth exceeded in markdown parsing");
		}

		var md = original_md.replaceAll("\r\n", "\n");
		StringBuilder mdBuilder = new StringBuilder(md);
		int offset = documentOffset;

		final List<LocatedBlock> blocks = new ArrayList<>();

		for (var blockRule : options.blockElementRules) {
			Block block = null;
			while ((block = blockRule.next(mdBuilder.toString())) != null) {

				if (block.start() != 0) {
					var before = mdBuilder.substring(0, block.start());
					blocks.addAll(tokenizeWithDepth(before, offset, depth + 1));
					offset += block.start();
				}

				blocks.add(new LocatedBlock(block, offset, offset + (block.end() - block.start())));
				offset += block.end() - block.start();
				mdBuilder.delete(0, block.end());
			}
		}

		return blocks;
	}
}
