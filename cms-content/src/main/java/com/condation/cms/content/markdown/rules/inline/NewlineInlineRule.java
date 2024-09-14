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
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class NewlineInlineRule implements InlineElementRule {
	
	private static final Pattern PATTERN = Pattern.compile(" {2,}\\n");


	
	@Override
	public InlineBlock next(String md) {
		var matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			return new NewlineInlineBlock(matcher.start(), matcher.end());
		}
		return null;
	}
	
	public static record NewlineInlineBlock(int start, int end) implements InlineBlock {
		@Override
		public String render() {
			return "<br/>";
		}
	}
	
}
