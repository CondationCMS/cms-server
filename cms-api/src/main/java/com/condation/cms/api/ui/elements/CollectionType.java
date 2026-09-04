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

/** A collection editor type registered with the manager UI. */
public record CollectionType(
		String name,
		String label,
		Map<String, FormDefinition> forms) {

	public CollectionType {
		name = ContentTypeDefinitionMapper.string(name, "<no name>");
		label = ContentTypeDefinitionMapper.string(label, name);
		forms = ContentTypeDefinitionMapper.copyForms(forms);
	}

	public CollectionType(String name, Map<String, FormDefinition> forms) {
		this(name, name, forms);
	}

	static CollectionType fromMap(Map<String, Object> collectionType) {
		var name = ContentTypeDefinitionMapper.string(collectionType.get("name"), "<no name>");
		return new CollectionType(
				name,
				ContentTypeDefinitionMapper.string(collectionType.get("label"), name),
				ContentTypeDefinitionMapper.forms(collectionType.get("forms")));
	}

	public FormDefinition getForm(String name) {
		return forms.getOrDefault(name, FormDefinition.empty());
	}
}
