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

import com.condation.cms.api.ui.elements.fields.CheckboxField;
import com.condation.cms.api.ui.elements.fields.CodeField;
import com.condation.cms.api.ui.elements.fields.ColorField;
import com.condation.cms.api.ui.elements.fields.DateField;
import com.condation.cms.api.ui.elements.fields.DateTimeField;
import com.condation.cms.api.ui.elements.fields.DividerField;
import com.condation.cms.api.ui.elements.fields.EasyMdeField;
import com.condation.cms.api.ui.elements.fields.EmailField;
import com.condation.cms.api.ui.elements.fields.FormField;
import com.condation.cms.api.ui.elements.fields.FormFieldChoice;
import com.condation.cms.api.ui.elements.fields.ListField;
import com.condation.cms.api.ui.elements.fields.MarkdownField;
import com.condation.cms.api.ui.elements.fields.MediaField;
import com.condation.cms.api.ui.elements.fields.NumberField;
import com.condation.cms.api.ui.elements.fields.RadioField;
import com.condation.cms.api.ui.elements.fields.RangeField;
import com.condation.cms.api.ui.elements.fields.ReferenceField;
import com.condation.cms.api.ui.elements.fields.SelectField;
import com.condation.cms.api.ui.elements.fields.StringField;
import com.condation.cms.api.ui.elements.fields.TagsField;
import com.condation.cms.api.ui.elements.fields.TextAreaField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts dynamic JavaScript hook input into typed, detached Java values. */
final class ContentTypeDefinitionMapper {

	private ContentTypeDefinitionMapper() {
	}

	static String string(Object value, String defaultValue) {
		return value instanceof String string ? string : defaultValue;
	}

	static boolean bool(Object value, boolean defaultValue) {
		return value instanceof Boolean bool ? bool : defaultValue;
	}

	static Map<String, FormDefinition> forms(Object value) {
		if (!(value instanceof Map<?, ?> formMap)) {
			return Map.of();
		}

		Map<String, FormDefinition> result = new LinkedHashMap<>();
		formMap.forEach((name, form) -> {
			if (name instanceof String stringName && form instanceof Map<?, ?> map) {
				result.put(stringName, FormDefinition.fromMap(objectMap(map)));
			}
		});
		return Collections.unmodifiableMap(result);
	}

	static Map<String, FormDefinition> copyForms(Map<String, FormDefinition> forms) {
		if (forms == null || forms.isEmpty()) {
			return Map.of();
		}
		Map<String, FormDefinition> result = new LinkedHashMap<>();
		forms.forEach((name, form) -> {
			if (name != null && form != null) {
				result.put(name, form);
			}
		});
		return Collections.unmodifiableMap(result);
	}

	static FormDefinition form(Object value) {
		if (!(value instanceof Map<?, ?> map)) {
			return FormDefinition.empty();
		}
		return FormDefinition.fromMap(objectMap(map));
	}

	static List<FormField> fields(Object value) {
		if (!(value instanceof Collection<?> fields)) {
			return List.of();
		}

		List<FormField> result = new ArrayList<>();
		for (Object field : fields) {
			if (field instanceof Map<?, ?> map) {
				result.add(field(objectMap(map)));
			}
		}
		return List.copyOf(result);
	}

	static List<FormField> copyFields(List<FormField> fields) {
		return fields == null || fields.isEmpty() ? List.of() : List.copyOf(fields);
	}

	private static FormField field(Map<String, Object> definition) {
		String type = string(definition.get("type"), "");
		String name = string(definition.get("name"), "");
		String title = string(definition.get("title"), "");
		boolean required = bool(definition.get("required"), false);
		String requiredMessage = string(definition.get("requiredMessage"), null);
		Map<String, Object> options = map(definition.get("options"));

		return switch (type) {
			case "text" -> StringField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.placeholder(string(definition.get("placeholder"), null)).build();
			case "textarea" -> TextAreaField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.rows(integer(definition.get("rows"))).build();
			case "email", "mail" -> EmailField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.placeholder(string(definition.get("placeholder"), null)).build();
			case "code" -> CodeField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.height(string(definition.get("height"), null)).build();
			case "markdown" -> MarkdownField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.placeholder(string(definition.get("placeholder"), null))
					.height(string(definition.get("height"), null)).build();
			case "easymde" -> EasyMdeField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage).build();
			case "number" -> NumberField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.min(number(options.get("min"))).max(number(options.get("max")))
					.step(number(options.get("step")))
					.placeholder(string(definition.get("placeholder"), null)).build();
			case "date" -> DateField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.placeholder(string(definition.get("placeholder"), null)).build();
			case "datetime" -> DateTimeField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.placeholder(string(definition.get("placeholder"), null)).build();
			case "color" -> ColorField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage).build();
			case "range" -> RangeField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.min(number(options.get("min"))).max(number(options.get("max")))
					.step(number(options.get("step"))).build();
			case "select" -> SelectField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.choices(choices(options.get("choices"))).build();
			case "radio" -> RadioField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.choices(choices(options.get("choices"))).build();
			case "checkbox" -> CheckboxField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.key(string(definition.get("key"), null))
					.choices(choices(options.get("choices"))).build();
			case "divider" -> new DividerField(name, title);
			case "media" -> MediaField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage).build();
			case "list" -> ListField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.nameField(string(options.get("nameField"), null)).build();
			case "reference" -> ReferenceField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.siteId(string(options.get("siteid"), null)).build();
			case "tags" -> TagsField.builder()
					.name(name).title(title).required(required).requiredMessage(requiredMessage)
					.taxonomy(string(options.get("taxonomy"), null)).build();
			default -> throw new IllegalArgumentException("Unsupported manager form field type: " + type);
		};
	}

	private static List<FormFieldChoice> choices(Object value) {
		if (!(value instanceof Collection<?> source)) {
			return List.of();
		}
		List<FormFieldChoice> result = new ArrayList<>();
		for (Object choice : source) {
			if (choice instanceof String string) {
				result.add(new FormFieldChoice(string));
			} else if (choice instanceof Map<?, ?> map) {
				Map<String, Object> choiceMap = objectMap(map);
				result.add(new FormFieldChoice(
						string(choiceMap.get("label"), ""),
						string(choiceMap.get("value"), "")));
			}
		}
		return List.copyOf(result);
	}

	private static Map<String, Object> map(Object value) {
		return value instanceof Map<?, ?> map ? objectMap(map) : Map.of();
	}

	private static Number number(Object value) {
		return value instanceof Number number ? number : null;
	}

	private static Integer integer(Object value) {
		return value instanceof Number number ? number.intValue() : null;
	}

	private static Map<String, Object> objectMap(Map<?, ?> source) {
		Map<String, Object> result = new LinkedHashMap<>();
		source.forEach((key, value) -> {
			if (key instanceof String stringKey) {
				result.put(stringKey, value);
			}
		});
		return result;
	}
}
