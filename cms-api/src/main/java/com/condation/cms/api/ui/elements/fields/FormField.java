package com.condation.cms.api.ui.elements.fields;

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

import lombok.Getter;

/** Base class for fields rendered by the manager form UI. */
@Getter
public abstract sealed class FormField permits
		CheckboxField,
		CodeField,
		ColorField,
		DateField,
		DateTimeField,
		DividerField,
		EasyMdeField,
		EmailField,
		ListField,
		MarkdownField,
		MediaField,
		NumberField,
		RadioField,
		RangeField,
		ReferenceField,
		SelectField,
		StringField,
		TagsField,
		TextAreaField {

	private final String type;
	private final String name;
	private final String title;
	private final boolean required;
	private final String requiredMessage;

	protected FormField(String type, String name, String title, boolean required, String requiredMessage) {
		this.type = type;
		this.name = name == null ? "" : name;
		this.title = title == null ? "" : title;
		this.required = required;
		this.requiredMessage = requiredMessage;
	}
}
