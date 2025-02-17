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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class TemplateLayoutTest extends AbstractTemplateEngineTest {

	private StringTemplateLoader templateLoader = new StringTemplateLoader();
	
	private Gson gson = new GsonBuilder()
			.setStrictness(Strictness.LENIENT)
			.create();

	@Override
	public TemplateLoader getLoader() {
		return templateLoader;
	}

	@ParameterizedTest
	@CsvSource({
		"base,child_1"
	})
	void test_layouts(String parent, String child) throws Exception {
		var parentTemplate = readContent(parent + ".html");
		var childTemplate = readContent(child + ".html");
		var expectedContent = readContent(child + "_expected.html");

		var data = getData(child);
		
		templateLoader.add(parent, parentTemplate);
		templateLoader.add(child, childTemplate);

		var template = SUT.getTemplate(child);

		var rendered = template.evaluate(data);

		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expectedContent);
	}

	private Map<String, Object> getData (String filename) throws IOException {
		String dataFile = filename + "_data.json";
		if (!exists(dataFile)) {
			return Collections.emptyMap();
		}
		
		var dataContent = readContent(dataFile);
		
		return gson.fromJson(dataContent, HashMap.class);
	}
	
	private boolean exists(String filename) {
		var resourcePath = "testdata/" + filename;
		var url = TemplateLayoutTest.class.getResource(resourcePath);
		return url != null;
	}

	private String readContent(String filename) throws IOException {
		try (var stream = TemplateLayoutTest.class.getResourceAsStream("testdata/layouts/" + filename);) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
