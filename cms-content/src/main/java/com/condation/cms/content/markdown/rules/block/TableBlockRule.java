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
import com.condation.cms.content.markdown.utils.StringUtils;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class TableBlockRule implements BlockElementRule {

	private static final Pattern PATTERN = Pattern.compile(
			"(^[\\|]{1}(.+?))((^\n)|\\Z)",
			Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern HEADER_DELIMITER = Pattern.compile("-{3,}");
	private static final Pattern ALIGN_LEFT = Pattern.compile(":-{3,}");
	private static final Pattern ALIGN_RIGHT = Pattern.compile("-{3,}:");
	private static final Pattern ALIGN_CENTER = Pattern.compile(":-{3,}:");

	@Override
	public Block next(String md) {
		Matcher matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			var content = matcher.group(1);

			var rows = content.split("\n");

			return new TableBlock(matcher.start(), matcher.end(),
					new Table(getColumns(rows), getHeader(rows), getRows(rows))
			);
		}
		return null;
	}

	private boolean isHeaderDelimiter(String row) {
		var items = row.split("\\/");
		for (var item : items) {
			if (HEADER_DELIMITER.matcher(item).find()) {
				return true;
			}
		}
		return false;
	}

	private Header getHeader(String[] rows) {
		if (rows.length > 2 && isHeaderDelimiter(rows[1])) {
			var row = StringUtils.removeTrailingPipe(
					StringUtils.removeLeadingPipe(rows[0])
			);
			var items = row.split("\\|");
			return new Header(Stream.of(items).map(String::trim).toList());
		}
		return new Header(List.of());
	}

	private List<Column> getColumns(String[] rows) {
		int index = -1;
		if (rows.length >= 1 && isHeaderDelimiter(rows[0])) {
			index = 0;
		} else if (rows.length >= 2 && isHeaderDelimiter(rows[1])) {
			index = 1;
		}
		if (index != -1) {
			var row = StringUtils.removeTrailingPipe(
					StringUtils.removeLeadingPipe(rows[index])
			);
			var items = row.split("\\|");
			return Stream.of(items)
					.map(String::trim)
					.map((item) -> {
						return Optional.ofNullable(mapAlign(item));
					})
					.map(align -> new Column(align))
					.toList();
		}
		return List.of();
	}

	private Align mapAlign(String value) {
		if (ALIGN_CENTER.matcher(value).matches()) {
			return Align.CENTER;
		} else if (ALIGN_RIGHT.matcher(value).matches()) {
			return Align.RIGHT;
		} else if (ALIGN_LEFT.matcher(value).matches()) {
			return Align.LEFT;
		}
		return null;
	}

	private List<Row> getRows(String[] rows) {
		int skip = 0;
		if (rows.length >= 1 && isHeaderDelimiter(rows[0])) {
			skip = 1;
		} else if (rows.length >= 2 && isHeaderDelimiter(rows[1])) {
			skip = 2;
		}

		return Stream.of(rows)
				.skip(skip)
				.map(row -> StringUtils.removeTrailingPipe(
				StringUtils.removeLeadingPipe(row))
				).map(row -> {
					var items = row.split("\\|");
					return new Row(Stream.of(items).map(String::trim).toList());
				}).toList();
	}

	static record Table(List<Column> columns, Header header, List<Row> rows) {

	}

	enum Align {
		CENTER,
		RIGHT,
		LEFT;
	}

	static record Header(List<String> values) {
	}

	static record Row(List<String> values) {

	}

	static record Column(Optional<Align> align) {
	}

	public static record TableBlock(int start, int end, Table table) implements Block {

		String renderAlign (int index) {
			if (table.columns.size() > index) {
				if (table.columns.get(index).align.isEmpty()) {
					return "";
				}
				
				return switch(table.columns.get(index).align.get()) {
					case CENTER -> "text-align: center";
					case LEFT -> "text-align: left";
					case RIGHT -> "text-align: right";
				};
			}
			return "";
		}
		
		private String renderStyle (int index) {
			var align = renderAlign(index);
			if (Strings.isNullOrEmpty(align)) {
				return "";
			}
			return "style=\"%s\"".formatted(align);
		}
		
		@Override
		public String render(InlineRenderer inlineRenderer) {
			StringBuilder sb = new StringBuilder();
			sb.append("<table>");
			
			if (!table.header.values.isEmpty()) {
				sb.append("<thead>");
				sb.append("<tr>");
				AtomicInteger index = new AtomicInteger(0);
				table.header.values.forEach((header) -> {
					sb.append("<th ").append(renderStyle(index.getAndIncrement())).append(">");
					sb.append(inlineRenderer.render(header));
					sb.append("</th>");
				});
				
				sb.append("</tr>");
				sb.append("</thead>");
			}
			
			sb.append("<tbody>");
			table.rows.forEach(row -> {
				AtomicInteger index = new AtomicInteger(0);
				sb.append("<tr>");
				row.values.forEach(items -> {
					sb.append("<td ").append(renderStyle(index.getAndIncrement())).append(">");
					sb.append(inlineRenderer.render(items));
					sb.append("</td>");
				});
				sb.append("</tr>");
			});
			sb.append("</tbody>");
			
			sb.append("</table>");
					
			return sb.toString();
		}
	}
}
