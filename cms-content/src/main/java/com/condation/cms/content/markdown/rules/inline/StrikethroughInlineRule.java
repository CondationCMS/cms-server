package com.condation.cms.content.markdown.rules.inline;

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

import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import com.condation.cms.content.markdown.InlineElementTokenizer;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author t.marx
 */
public class StrikethroughInlineRule implements InlineElementRule {
	
    private static final Pattern PATTERN = Pattern.compile("(?<selector>~{2})(?<content>.*?)(?<!\\\\)(\\k<selector>)");

    @Override
    public InlineBlock next(InlineElementTokenizer tokenizer, String md) {
        var matcher = PATTERN.matcher(md);
        if (matcher.find()) {
            return new StrikethroughInlineBlock(tokenizer, matcher.start(), matcher.end(), matcher.group("content"));
        }
        return null;
    }

    public static record StrikethroughInlineBlock(InlineElementTokenizer tokenizer, int start, int end, String content) implements InlineBlock {

        @Override
        public String render() {
            try {
                var renderedContent = tokenizer.tokenize(content).stream().map(b -> b.block().render()).collect(Collectors.joining());
                return "<del>%s</del>".formatted(renderedContent);
            } catch (IOException ex) {
                return "<del>%s</del>".formatted(content);
            }
        }
    }
	
}
