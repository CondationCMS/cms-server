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
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.IfTag;
import com.condation.cms.templates.tags.IncludeTag;
import com.condation.cms.templates.tags.SetTag;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineIncludeTest {

	TemplateEngine templateEngine;

	@BeforeEach
	void setupTemplateEngine() {
		TemplateConfiguration config = new TemplateConfiguration();
		config
				.registerTag(new IncludeTag());

		config.setTemplateLoader(new StringTemplateLoader()
				.add("simple1", """
                   {% include "temp1" %}
                   """)
				.add("simple2", """
                   {% include "nested/temp2" %}
                   """)
				.add("temp1", """
                  This is from template1
				""")
				.add("nested/temp2", """
                  This is from template2
                         """)
		);

		this.templateEngine = new TemplateEngine(config);
	}

	@Test
	public void test_template1() {
		Template simpleTemplate = templateEngine.getTemplate("simple1");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.execute()).isEqualToIgnoringWhitespace("This is from template1");
	}

	@Test
	public void test_template2() {
		Template simpleTemplate = templateEngine.getTemplate("simple2");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.execute()).isEqualToIgnoringWhitespace("This is from template2");
	}
}
