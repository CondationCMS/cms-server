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


import com.condation.cms.content.markdown.rules.block.BlockquoteBlockRule;
import com.condation.cms.content.markdown.rules.block.CodeBlockRule;
import com.condation.cms.content.markdown.rules.block.DefinitionListBlockRule;
import com.condation.cms.content.markdown.rules.block.HeadingBlockRule;
import com.condation.cms.content.markdown.rules.block.HorizontalRuleBlockRule;
import com.condation.cms.content.markdown.rules.block.ListBlockRule;
import com.condation.cms.content.markdown.rules.block.ShortCodeBlockRule;
import com.condation.cms.content.markdown.rules.block.TableBlockRule;
import com.condation.cms.content.markdown.rules.block.TaskListBlockRule;
import com.condation.cms.content.markdown.rules.inline.HighlightInlineRule;
import com.condation.cms.content.markdown.rules.inline.ImageInlineRule;
import com.condation.cms.content.markdown.rules.inline.ImageLinkInlineRule;
import com.condation.cms.content.markdown.rules.inline.ItalicInlineRule;
import com.condation.cms.content.markdown.rules.inline.LinkInlineRule;
import com.condation.cms.content.markdown.rules.inline.NewlineInlineRule;
import com.condation.cms.content.markdown.rules.inline.ShortCodeInlineBlockRule;
import com.condation.cms.content.markdown.rules.inline.StrikethroughInlineRule;
import com.condation.cms.content.markdown.rules.inline.StrongInlineRule;
import com.condation.cms.content.markdown.rules.inline.SubscriptInlineRule;
import com.condation.cms.content.markdown.rules.inline.SuperscriptInlineRule;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class Options {
	
	public static Options all () {
		Options options = new Options();
		options.addInlineRule(new ShortCodeInlineBlockRule());
		options.addInlineRule(new StrongInlineRule());
		options.addInlineRule(new ItalicInlineRule());
		options.addInlineRule(new NewlineInlineRule());
		options.addInlineRule(new ImageLinkInlineRule());
		options.addInlineRule(new ImageInlineRule());
		options.addInlineRule(new LinkInlineRule());
		options.addInlineRule(new StrikethroughInlineRule());
		options.addInlineRule(new HighlightInlineRule());
		options.addInlineRule(new SubscriptInlineRule());
		options.addInlineRule(new SuperscriptInlineRule());

		options.addBlockRule(new ShortCodeBlockRule());
		options.addBlockRule(new CodeBlockRule());
		options.addBlockRule(new HeadingBlockRule());
		options.addBlockRule(new TaskListBlockRule());
		options.addBlockRule(new ListBlockRule());
		options.addBlockRule(new HorizontalRuleBlockRule());
		options.addBlockRule(new BlockquoteBlockRule());
		options.addBlockRule(new TableBlockRule());
		options.addBlockRule(new DefinitionListBlockRule());
		
		return options;
	}
	
	public List<BlockElementRule> blockElementRules = new ArrayList<>();
	public List<InlineElementRule> inlineElementRules = new ArrayList<>();
	
	public void addBlockRule (final BlockElementRule blockElementRule) {
		blockElementRules.add(blockElementRule);
	}
	
	public void addInlineRule (final InlineElementRule inlineElementRule) {
		inlineElementRules.add(inlineElementRule);
	}
}
