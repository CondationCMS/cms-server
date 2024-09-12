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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class InlineElementTokenizer {

	private final Options options;

	protected List<InlineBlock> tokenize(final String original_md) throws IOException {

		var md = original_md.replaceAll("\r\n", "\n");
		StringBuilder mdBuilder = new StringBuilder(md);

		final List<InlineBlock> blocks = new ArrayList<>();

		for (var blockRule : options.inlineElementRules) {
			InlineBlock block = null;
			while ((block = blockRule.next(mdBuilder.toString())) != null) {

				if (block.start() != 0) {
					var before = mdBuilder.substring(0, block.start());
					blocks.addAll(tokenize(before));
				}

				blocks.add(block);
				mdBuilder.delete(0, block.end());
			}
		}

		return blocks;
	}
}
