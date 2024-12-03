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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineBlockTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("parent", """
                   {% block title %}
						Default title
                   {% endblock  %}
                   """)
				.add("child_without_block", """
                   {% extends "parent" %}
                   """)
				.add("child_with_block", """
                   {% extends "parent" %}
                   {% block title %}
                      Custom title
                   {% endblock %}
                   """);
	}

	@Test
	public void test_without_block() throws IOException {
		Template simpleTemplate = SUT.getTemplate("child_without_block");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate()).isEqualToIgnoringWhitespace("Default title");
	}

	@Test
	public void test_with_block() throws IOException {
		Template simpleTemplate = SUT.getTemplate("child_with_block");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate()).isEqualToIgnoringWhitespace("Custom title");
	}
}
