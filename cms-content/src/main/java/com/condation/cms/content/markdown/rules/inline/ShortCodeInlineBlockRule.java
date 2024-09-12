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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class ShortCodeInlineBlockRule implements InlineElementRule {

	public static final Pattern TAG_PARAMS_PATTERN_SHORT = Pattern.compile("(\\[{2})(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\p{Blank}*/\\]{2}",
			Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES);
	public static final Pattern TAG_PARAMS_PATTERN_LONG = Pattern.compile("^(\\[{2})(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\]{2}(?<content>.*)\\[{2}/\\k<tag>\\]{2}",
			Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES);
	
	@Override
	public InlineBlock next(final String md) {
		Matcher matcher = TAG_PARAMS_PATTERN_SHORT.matcher(md);
		if (matcher.find()) {
			return new ShortCodeInlineBlock(matcher.start(), matcher.end(), 
					matcher.group("tag"), matcher.group("params"), ""
			);
		}
		matcher = TAG_PARAMS_PATTERN_LONG.matcher(md);
		if (matcher.find()) {
			return new ShortCodeInlineBlock(matcher.start(), matcher.end(), 
					matcher.group("tag"), matcher.group("params"), matcher.group("content")
			);
		}
		return null;
	}

	
	public static record ShortCodeInlineBlock (int start, int end, String tag, String params, String content) implements InlineBlock {

		@Override
		public String render() {
			return "[[%s %s]]%s[[/%s]]".formatted(tag, params, content, tag);
		}
		
		
		
	}
	
}
