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

import lombok.Builder;
import lombok.Getter;

@Getter
public final class NumberField extends FormField {

	private final Options options;
	private final String placeholder;

	@Builder
	public NumberField(String name, String title, boolean required, String requiredMessage,
			Number min, Number max, Number step, String placeholder) {
		super("number", name, title, required, requiredMessage);
		this.options = new Options(min, max, step);
		this.placeholder = placeholder;
	}

	public NumberField(String name, String title) {
		this(name, title, false, null, null, null, null, null);
	}

	public NumberField(String name, String title, Number min, Number max, Number step) {
		this(name, title, false, null, min, max, step, null);
	}

	public record Options(Number min, Number max, Number step) {
	}
}
