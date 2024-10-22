package com.condation.cms.tests.mustache;

/*-
 * #%L
 * tests
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

import com.github.jknack.handlebars.Handlebars;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class HandlebarTest {

	public static void main(String... args) throws Exception {
		String template = """
                    {{ name }}, {{ feature.description }}!
                    - {{#listContent}}"{{.}}", {{/listContent}}
                    + {{ mapContent.name }}
                    """;

		HashMap<String, Object> scopes = new HashMap<>();
		scopes.put("name", "Mustache");
		scopes.put("feature", new Feature("Perfect!"));
		scopes.put("listContent", List.of("Hallo", "World", "!"));
		scopes.put("mapContent", Map.of("name", "CondationCMS"));

		Handlebars handlebars = new Handlebars();
		var compiled = handlebars.compileInline(template);
		System.out.println(compiled.apply(scopes));
	}

	public static record Feature(String description) {

	}
;
}
