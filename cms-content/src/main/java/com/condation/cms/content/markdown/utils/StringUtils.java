package com.condation.cms.content.markdown.utils;

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
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class StringUtils {

	private static final Map<String, String> ESCAPE = new HashMap<>();

	private static final String AMP_PLACEHOLDER = "AMP#PLACE#HOLDER";

	static {
		ESCAPE.put("\\#", AMP_PLACEHOLDER + "#35;");
		ESCAPE.put("\\*", AMP_PLACEHOLDER + "#42;");
		ESCAPE.put("\\`", AMP_PLACEHOLDER + "#96;");
		ESCAPE.put("\\_", AMP_PLACEHOLDER + "#95;");
		ESCAPE.put("\\{", AMP_PLACEHOLDER + "#123;");
		ESCAPE.put("\\}", AMP_PLACEHOLDER + "#125;");
		ESCAPE.put("\\[", AMP_PLACEHOLDER + "#91;");
		ESCAPE.put("\\]", AMP_PLACEHOLDER + "#93;");
		ESCAPE.put("\\<", AMP_PLACEHOLDER + "#60;");
		ESCAPE.put("\\>", AMP_PLACEHOLDER + "#62;");
		ESCAPE.put("\\(", AMP_PLACEHOLDER + "#40;");
		ESCAPE.put("\\)", AMP_PLACEHOLDER + "#41;");
		ESCAPE.put("\\+", AMP_PLACEHOLDER + "#43;");
		ESCAPE.put("\\-", AMP_PLACEHOLDER + "#45;");
		ESCAPE.put("\\.", AMP_PLACEHOLDER + "#46;");
		ESCAPE.put("\\!", AMP_PLACEHOLDER + "#33;");
		ESCAPE.put("\\|", AMP_PLACEHOLDER + "#124;");
	}

	public static String unescape(String html) {
		return html.replaceAll(AMP_PLACEHOLDER, "&");
	}

	public static String escape(String md) {
		if (Strings.isNullOrEmpty(md)) {
			return md;
		}
		StringBuilder escaped = new StringBuilder(md);

		ESCAPE.forEach((key, value) -> {
			int index = escaped.indexOf(key);
			while (index != -1) {
				escaped.replace(index, index + key.length(), value);
				index = escaped.indexOf(key, index + value.length());
			}
		});

		return escaped.toString();
	}

	public static String removeLeadingPipe(String s) {
		return s.replaceAll("^\\|+", "");
	}

	public static String removeTrailingPipe(String s) {
		return s.replaceAll("\\|++$", "");
	}
}
