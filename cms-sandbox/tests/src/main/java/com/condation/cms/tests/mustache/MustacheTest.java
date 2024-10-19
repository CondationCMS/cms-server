package com.condation.cms.tests.mustache;

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
public class MustacheTest {

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

		Writer writer = new OutputStreamWriter(System.out);
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader(template), "example");
		mustache.execute(writer, scopes);
		writer.flush();
	}
	
	public static record Feature (String description) {};
}
