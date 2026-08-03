package com.condation.cms.api.ui.elements;

/*-
 * #%L
 * CMS Api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ui.elements.fields.FormField;
import com.condation.cms.api.ui.elements.fields.MarkdownField;
import com.condation.cms.api.ui.elements.fields.NumberField;
import com.condation.cms.api.ui.elements.fields.StringField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypesTest {

	@Test
	void convertsDynamicRegistrationIntoDetachedTypedDefinition() {
		Map<String, Object> options = new HashMap<>(Map.of("min", 1));
		Map<String, Object> field = new HashMap<>(Map.of(
				"type", "number",
				"name", "count",
				"options", options));
		List<Map<String, Object>> fields = new ArrayList<>(List.of(field));
		Map<String, Object> tabField = new HashMap<>(Map.of(
				"type", "text",
				"name", "description"));
		List<Map<String, Object>> tabFields = new ArrayList<>(List.of(tabField));
		Map<String, Object> tab = new HashMap<>(Map.of(
				"title", "Details",
				"fields", tabFields));
		Map<String, Object> settings = new HashMap<>(Map.of(
				"fields", fields,
				"tabs", new ArrayList<>(List.of(tab))));
		Map<String, Object> input = new HashMap<>(Map.of(
				"name", "StartPage",
				"template", "start.html",
				"contentFolder", "content",
				"createButton", false,
				"forms", Map.of("settings", settings)));

		ContentTypes contentTypes = new ContentTypes();
		contentTypes.registerPageTemplate(input);

		PageTemplate pageTemplate = contentTypes.getPageTemplate("StartPage").orElseThrow();
		assertThat(pageTemplate).isInstanceOf(PageTemplate.class);
		assertThat(pageTemplate.template()).isEqualTo("start.html");
		assertThat(pageTemplate.contentFolder()).isEqualTo("content");
		assertThat(pageTemplate.createButton()).isFalse();
		assertThat(pageTemplate.getForm("settings").fields())
				.singleElement()
				.isInstanceOfSatisfying(NumberField.class, numberField -> {
					assertThat(numberField.getName()).isEqualTo("count");
					assertThat(numberField.getOptions().min()).isEqualTo(1);
				});
		assertThat(pageTemplate.getForm("settings").tabs())
				.singleElement()
				.satisfies(storedTab -> {
					assertThat(storedTab.title()).isEqualTo("Details");
					assertThat(storedTab.fields()).singleElement()
							.isInstanceOfSatisfying(StringField.class,
									storedTabField -> assertThat(storedTabField.getName()).isEqualTo("description"));
				});

		input.put("template", "changed.html");
		field.put("name", "changed");
		options.put("min", 99);
		tab.put("title", "Changed");
		tabField.put("name", "changed");

		assertThat(pageTemplate.template()).isEqualTo("start.html");
		NumberField storedField = (NumberField) pageTemplate.getForm("settings").fields().getFirst();
		assertThat(storedField.getName()).isEqualTo("count");
		assertThat(storedField.getOptions().min()).isEqualTo(1);
		assertThat(pageTemplate.getForm("settings").tabs().getFirst().title()).isEqualTo("Details");
		assertThat(pageTemplate.getForm("settings").tabs().getFirst().fields().getFirst().getName())
				.isEqualTo("description");
	}

	@Test
	void supportsTypedJavaRegistration() {
		FormDefinition form = new FormDefinition(
				List.of(new StringField("title", "Title")),
				List.of(new FormTab("Details", List.of(
						new NumberField("count", "Count", 0, 100, 1),
						new MarkdownField("description", "Description")))));
		PageTemplate pageTemplate = PageTemplate.builder()
				.name("Default")
				.template("default.html")
				.forms(Map.of("settings", form))
				.contentFolder("content")
				.build();
		SectionEntryTemplate sectionEntryTemplate = SectionEntryTemplate.builder()
				.section("main")
				.name("Hero")
				.template("hero.html")
				.forms(Map.of("attributes", form))
				.build();
		ListItemType listItemType = new ListItemType("features", form);

		ContentTypes contentTypes = new ContentTypes();
		contentTypes.registerPageTemplate(pageTemplate);
		contentTypes.registerSectionEntryTemplate(sectionEntryTemplate);
		contentTypes.registerListItemType(listItemType);

		assertThat(contentTypes.getPageTemplates()).containsExactly(pageTemplate);
		assertThat(contentTypes.getSectionEntryTemplates("main")).containsExactly(sectionEntryTemplate);
		assertThat(contentTypes.getListItemTypes()).containsExactly(listItemType);
		assertThat(pageTemplate.createButton()).isTrue();
		assertThat(form.fields()).allMatch(FormField.class::isInstance);
		assertThat(form.tabs()).singleElement()
				.satisfies(tab -> assertThat(tab.fields()).allMatch(FormField.class::isInstance));
	}
}
