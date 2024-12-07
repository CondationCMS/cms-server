package com.condation.cms.templates;

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

import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineFORTest extends AbstractTemplateEngineTest {

	
	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("simple", """
                   {% for name in names %}
						<li>{{ name }}</li>
                   {% endfor %}
                   """)
				.add("index", """
                   {% for name in names %}
						<li>{{ loop.getIndex() }}</li>
                   {% endfor %}
                   """);
	}
	
	@Test
	public void test_for() throws IOException {
		
		var expected = """
                 <li>one</li>
                 <li>two</li>
                 <li>three</li>
                 """;
		
		Template simpleTemplate = SUT.getTemplate("simple");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Map<String, Object> context = Map.of("names", List.of("one", "two", "three"));
		Assertions.assertThat(simpleTemplate.evaluate(context)).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	public void test_index() throws IOException {
		
		var expected = """
                 <li>0</li>
                 <li>1</li>
                 <li>2</li>
                 """;
		
		Template simpleTemplate = SUT.getTemplate("index");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Map<String, Object> context = Map.of("names", List.of("one", "two", "three"));
		Assertions.assertThat(simpleTemplate.evaluate(context)).isEqualToIgnoringWhitespace(expected);
	}
	
}
