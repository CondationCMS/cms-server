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
public class TaskListBlockRule implements BlockElementRule {

	private static final Pattern PATTERN = Pattern.compile(
			"(^- \\[([ x]?)\\] (.+?)\n)+((^\n)|\\Z)",
			Pattern.MULTILINE);

	private static final Pattern CHECKED_PATTERN = Pattern.compile(
			"- \\[(?<checked>[ x]?)\\] (?<content>.+?)");

	@Override
	public Block next(String md) {
		Matcher matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			var content = matcher.group(0).trim();

			return new TaskListBlock(matcher.start(), matcher.end(),
					createTaskList(content)
			);
		}
		return null;
	}

	private TaskList createTaskList(String content) {
		var rows = content.split("\n");
		List<Item> definitionLists = new ArrayList<>();
		for (var row : rows) {
			if ("".equals(row.trim())) {
				continue;
			}
			var matcher = CHECKED_PATTERN.matcher(row);

			if (matcher.matches()) {
				definitionLists.add(
						new Item(
								matcher.group("content").trim(),
								"x".equalsIgnoreCase(matcher.group("checked"))
						)
				);
			}
		}

		return new TaskList(definitionLists);
	}

	static record TaskList(List<Item> items) {

	}

	static record Item(String title, boolean checked) {

	}

	public static record TaskListBlock(int start, int end, TaskList taskList) implements Block {

		@Override
		public String render(InlineRenderer inlineRenderer) {
			StringBuilder sb = new StringBuilder();
			sb.append("<ul>");

			taskList.items().forEach(item -> {
				sb.append("<li>");

				sb.append("<input disabled=\"\" type=\"checkbox\" ");
				if (item.checked()) {
					sb.append("checked=\"\"");
				}
				sb.append("/> ");

				sb.append(item.title());
				sb.append("</li>");
			});

			sb.append("</ul>");

			return sb.toString();
		}
	}
}
