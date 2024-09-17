package com.condation.cms.content.markdown.rules.block;

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

import com.condation.cms.content.markdown.Block;
import com.condation.cms.content.markdown.BlockElementRule;
import com.condation.cms.content.markdown.InlineRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class DefinitionListBlockRule implements BlockElementRule {

	private static final Pattern PATTERN = Pattern.compile(
			"(?<content>(^.+?\n(: .+?(\n|\\Z))+)+)", 
			Pattern.MULTILINE);


	@Override
	public Block next(String md) {
		Matcher matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			var content = matcher.group(0).trim();
			

			return new DefinitionListBlock(matcher.start(), matcher.end(),
					createContainer(content)
			);
		}
		return null;
	}

	private DefinitionListContainer createContainer (String content) {
		var rows = content.split("\n");
		String currentTitle = null;
		List<String> currentItems = null;
		List<DefinitionList> definitionLists = new ArrayList<>();
		for (var row : rows) {
			if (row.startsWith(": ")) {
				var item = row.replaceFirst(":", "").trim();
				if (currentItems == null){
					currentItems = new ArrayList<>();
				}
				currentItems.add(item);
			} else if (currentTitle == null) {
				currentTitle = row.trim();
			} else if (currentTitle != null) {
				definitionLists.add(new DefinitionList(currentTitle, currentItems));
				currentTitle = row.trim();
				currentItems = null;
			}
		}
		if (currentTitle != null) {
			definitionLists.add(new DefinitionList(currentTitle, currentItems));
		}
		
		return new DefinitionListContainer(definitionLists);
	}
	
	static record DefinitionListContainer(List<DefinitionList> lists) {
	}

	static record DefinitionList(String title, List<String> values) {

	}

	
	public static record DefinitionListBlock(int start, int end, DefinitionListContainer listContainer) implements Block {

		@Override
		public String render(InlineRenderer inlineRenderer) {
			StringBuilder sb = new StringBuilder();
			sb.append("<dl>");
			
			listContainer.lists().forEach(list -> {
				sb.append("<dt>").append(inlineRenderer.render(list.title())).append("</dt>");
				
				list.values.forEach(item -> {
					sb.append("<dd>").append(inlineRenderer.render(item)).append("</dd>");
				});
			});
			
			sb.append("</dl>");
					
			return sb.toString();
		}
	}
}
