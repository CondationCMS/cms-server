package com.condation.cms.content.markdown.rules.inline;

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


import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import com.google.common.base.Strings;

/**
 *
 * @author t.marx
 */
public class TextInlineRule implements InlineElementRule {
	
	@Override
	public InlineBlock next(String md) {
		if (Strings.isNullOrEmpty(md)) {
			return null;
		}
		return new TextBlock(0, md.length(), md);
	}
	
	public static record TextBlock(int start, int end, String content) implements InlineBlock {
		@Override
		public String render() {
			return content;
		}
	}
}
