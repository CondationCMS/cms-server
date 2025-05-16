package com.condation.cms.templates.filter.impl;

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

import com.condation.cms.templates.filter.Filter;

public class DefaultFilter implements Filter {

    public static final String NAME = "default";

@Override
public Object apply(Object input, Object... params) {
	if (input == null) {
        return params.length == 1 ? params[0] : null;
    }

    String str = input.toString().trim();

    // Prüfe auf escaped leeres <p></p>
    if (str.isEmpty() 
			|| str.equals("&lt;p&gt;&lt;/p&gt;") 
			|| str.equals("&amp;lt;p&amp;gt;&amp;lt;/p&amp;gt;")
			|| str.equals("<p></p>")) {
        return params.length == 1 ? params[0] : input;
    }

    return input;
}

}
