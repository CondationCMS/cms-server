package com.github.thmarx.cms.content.markdown.rules.inline;

/*-
 * #%L
 * cms-markdown
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import com.github.thmarx.cms.content.markdown.InlineBlock;
import com.github.thmarx.cms.content.markdown.InlineElementRule;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class StrikethroughInlineRule implements InlineElementRule {
	
	private static final Pattern PATTERN = Pattern.compile("(~{2})(?<content>.*?)(~{2})");
	
	@Override
	public InlineBlock next(String md) {
		var matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			return new StrikethroughInlineBlock(matcher.start(), matcher.end(), matcher.group("content"));
		}
		return null;
	}
	
	public static record StrikethroughInlineBlock(int start, int end, String content) implements InlineBlock {

		@Override
		public String render() {
			return "<del>%s</del>".formatted(content);
		}
	}
	
}
