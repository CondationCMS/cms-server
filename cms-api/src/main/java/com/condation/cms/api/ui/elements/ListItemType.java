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

/** A globally registered form for items of a list field. */
@Builder
public record ListItemType(String name, FormDefinition form) {

	public ListItemType {
		name = ContentTypeDefinitionMapper.string(name, "<no name>");
		form = form == null ? FormDefinition.empty() : form;
	}

	static ListItemType fromMap(Map<String, Object> listItemType) {
		return new ListItemType(
				ContentTypeDefinitionMapper.string(listItemType.get("name"), "<no name>"),
				ContentTypeDefinitionMapper.form(listItemType.get("form")));
	}

	public FormDefinition getForm() {
		return form;
	}
}
