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

import java.util.Map;
import lombok.Builder;

/** A section entry type registered with the manager UI. */
@Builder
public record SectionEntryTemplate(
		String section,
		String name,
		String template,
		Map<String, FormDefinition> forms) {

	public SectionEntryTemplate {
		section = ContentTypeDefinitionMapper.string(section, "<no section>");
		name = ContentTypeDefinitionMapper.string(name, "<no name>");
		template = ContentTypeDefinitionMapper.string(template, "<no template>");
		forms = ContentTypeDefinitionMapper.copyForms(forms);
	}

	public SectionEntryTemplate(String section, String name, String template) {
		this(section, name, template, Map.of());
	}

	static SectionEntryTemplate fromMap(Map<String, Object> sectionEntryTemplate) {
		return new SectionEntryTemplate(
				ContentTypeDefinitionMapper.string(sectionEntryTemplate.get("section"), "<no section>"),
				ContentTypeDefinitionMapper.string(sectionEntryTemplate.get("name"), "<no name>"),
				ContentTypeDefinitionMapper.string(sectionEntryTemplate.get("template"), "<no template>"),
				ContentTypeDefinitionMapper.forms(sectionEntryTemplate.get("forms")));
	}

	public FormDefinition getForm(String name) {
		return forms.getOrDefault(name, FormDefinition.empty());
	}
}
