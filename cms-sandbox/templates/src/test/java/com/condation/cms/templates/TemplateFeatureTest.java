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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class TemplateFeatureTest {
	
	private StringTemplateLoader templateLoader = new StringTemplateLoader();
	
	private TemplateEngine SUT = TemplateEngineBuilder.buildDefault(templateLoader);
	
	@Test
	void test_variable_replacement () throws Exception {
		var templateFile = "variable_1.html";
		var templateContent = readContent(templateFile);
		var expectedContent = readContent("variable_1_expected.html");
		
		templateLoader.add(templateFile, templateContent);
		
		var template = SUT.getTemplate(templateFile);
	
		var rendered = template.execute(Map.of("name", "CondationCMS"));
		
		Assertions.assertThat(rendered).isEqualTo(expectedContent);
	}

	@ParameterizedTest
	@CsvSource({
		"variable_raw_filter.html,variable_raw_filter_expected.html" 
	})
	void test_features (String templateFile, String expectedFile) throws Exception {
		var templateContent = readContent(templateFile);
		var expectedContent = readContent(expectedFile);
		
		templateLoader.add(templateFile, templateContent);
		
		var template = SUT.getTemplate(templateFile);
	
		var rendered = template.execute();
		
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expectedContent);
	}
	
	private String readContent (String filename) throws IOException {
		try (var stream = TemplateFeatureTest.class.getResourceAsStream("testdata/" + filename);) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
