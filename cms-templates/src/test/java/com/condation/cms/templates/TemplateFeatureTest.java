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
import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.content.shortcodes.TagParser;
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
public class TemplateFeatureTest extends AbstractTemplateEngineTest {

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
		"variable_raw_filter",
		"variable_1",
		"for_1",
		"if_1",
		"component_1"
	})
	void test_features(String templateFile) throws Exception {
		var templateContent = readContent(templateFile + ".html");
		var expectedContent = readContent(templateFile + "_expected.html");

		var data = getData(templateFile);
		
		templateLoader.add(templateFile, templateContent);

		var template = SUT.getTemplate(templateFile);

		var rendered = template.evaluate(data, createDynamicConfig());

		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expectedContent);
	}

	private DynamicConfiguration createDynamicConfig () {
		ShortCodes shortCodes = new ShortCodes(
				Map.of(
						"hello", (params) -> "hello " + params.get("name")
				), 
				new TagParser(null));
		return new DynamicConfiguration(shortCodes);
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
		var url = TemplateFeatureTest.class.getResource(resourcePath);
		return url != null;
	}

	private String readContent(String filename) throws IOException {
		try (var stream = TemplateFeatureTest.class.getResourceAsStream("testdata/" + filename);) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
