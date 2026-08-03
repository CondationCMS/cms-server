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
import java.util.List;
import java.util.Map;
import lombok.Builder;

/** A named group of fields rendered as one tab of a manager form. */
@Builder
public record FormTab(String title, List<FormField> fields) {

	public FormTab {
		title = ContentTypeDefinitionMapper.string(title, "");
		fields = ContentTypeDefinitionMapper.copyFields(fields);
	}

	static FormTab fromMap(Map<String, Object> tab) {
		return new FormTab(
				ContentTypeDefinitionMapper.string(tab.get("title"), ""),
				ContentTypeDefinitionMapper.fields(tab.get("fields")));
	}
}
