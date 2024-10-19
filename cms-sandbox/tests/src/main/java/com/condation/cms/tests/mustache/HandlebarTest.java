package com.condation.cms.tests.mustache;

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
