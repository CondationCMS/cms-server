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

/** A page type registered with the manager UI. */
public record PageTemplate(
		String name,
		String template,
		Map<String, FormDefinition> forms,
		String contentFolder,
		boolean createButton) {

	public PageTemplate {
		name = ContentTypeDefinitionMapper.string(name, "<no name>");
		template = ContentTypeDefinitionMapper.string(template, "<no template>");
		forms = ContentTypeDefinitionMapper.copyForms(forms);
		contentFolder = ContentTypeDefinitionMapper.string(contentFolder, "");
	}

	public PageTemplate(String name, String template) {
		this(name, template, Map.of(), "", true);
	}

	public PageTemplate(String name, String template, Map<String, FormDefinition> forms) {
		this(name, template, forms, "", true);
	}

	public static Builder builder() {
		return new Builder();
	}

	static PageTemplate fromMap(Map<String, Object> pageTemplate) {
		return new PageTemplate(
				ContentTypeDefinitionMapper.string(pageTemplate.get("name"), "<no name>"),
				ContentTypeDefinitionMapper.string(pageTemplate.get("template"), "<no template>"),
				ContentTypeDefinitionMapper.forms(pageTemplate.get("forms")),
				ContentTypeDefinitionMapper.string(pageTemplate.get("contentFolder"), ""),
				ContentTypeDefinitionMapper.bool(pageTemplate.get("createButton"), true));
	}

	public FormDefinition getForm(String name) {
		return forms.getOrDefault(name, FormDefinition.empty());
	}

	public String getContentFolder() {
		return contentFolder;
	}

	public boolean addCreateButton() {
		return createButton;
	}

	public static final class Builder {

		private String name;
		private String template;
		private Map<String, FormDefinition> forms = Map.of();
		private String contentFolder = "";
		private boolean createButton = true;

		private Builder() {
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder template(String template) {
			this.template = template;
			return this;
		}

		public Builder forms(Map<String, FormDefinition> forms) {
			this.forms = forms;
			return this;
		}

		public Builder contentFolder(String contentFolder) {
			this.contentFolder = contentFolder;
			return this;
		}

		public Builder createButton(boolean createButton) {
			this.createButton = createButton;
			return this;
		}

		public PageTemplate build() {
			return new PageTemplate(name, template, forms, contentFolder, createButton);
		}
	}
}
