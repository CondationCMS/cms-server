package com.condation.cms.templates.utils;

/*-
 * #%L
 * templates
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

import com.condation.cms.templates.tags.macro.MacroTag;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class MacroUtils {
	public static final Pattern MACRO_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\((.*)\\)");
	
	public static Optional<MacroTag.Macro> parseMacro(String expression) {
		if (Strings.isNullOrEmpty(expression)) {
			return Optional.empty();
		}
		expression = expression.trim();
        Matcher matcher = MACRO_PATTERN.matcher(expression);

        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String params = matcher.group(2).trim();

            List<String> paramList = new ArrayList<>();
            if (!params.isEmpty()) {
                paramList = Arrays.asList(params.split("\\s*,\\s*"));
            }

            return Optional.of(new MacroTag.Macro(methodName, paramList));
        }
		
		return Optional.empty();
    }
}
