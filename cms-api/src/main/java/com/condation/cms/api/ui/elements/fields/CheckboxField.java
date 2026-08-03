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

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class CheckboxField extends FormField {

	private final String key;
	private final Options options;

	@Builder
	public CheckboxField(String name, String title, boolean required, String requiredMessage,
			String key, List<FormFieldChoice> choices) {
		super("checkbox", name, title, required, requiredMessage);
		this.key = key;
		this.options = new Options(choices);
	}

	public CheckboxField(String name, String title, List<FormFieldChoice> choices) {
		this(name, title, false, null, null, choices);
	}

	public record Options(List<FormFieldChoice> choices) {
		public Options {
			choices = choices == null ? List.of() : List.copyOf(choices);
		}
	}
}
