package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
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

import java.util.Objects;

/**
 *
 * @author thorstenmarx
 */
public class MarkdownHelper {

	public static String replaceRange(String markdown,
			int start,
			int end,
			String replacement) {

		Objects.requireNonNull(markdown);
		Objects.requireNonNull(replacement);

		// step is necessary because it is also in markdown renderer
		markdown = markdown.replace("\r\n", "\n");
		
		if (start < 0 || end < start || end > markdown.length()) {
			throw new IllegalArgumentException(
					"Invalid range: start=" + start + ", end=" + end);
		}

		StringBuilder sb = new StringBuilder(
				markdown.length() - (end - start) + replacement.length());

		sb.append(markdown, 0, start);
		sb.append(replacement);
		sb.append(markdown, end, markdown.length());

		return sb.toString();
	}
}
