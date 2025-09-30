/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import { createID } from "./utils.js";
import { i18n } from "../localization.js"
import { FieldOptions, FormContext, FormField } from "./forms.js";

export interface ColorFieldOptions extends FieldOptions {
}

const createColorField = (options: ColorFieldOptions, value = '#000000') => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)

	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="color">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="color" class="form-control form-control-color" id="${id}" name="${options.name}" value="${value || '#000000'}" title="${title}">
		</div>
	`;
};

const getColorData = (context : FormContext) => {
	const data = {};
	context.formElement.querySelectorAll("[data-cms-form-field-type='color'] input").forEach((el: HTMLInputElement )  => {
		data[el.name] = {
			type: 'color',
			value: el.value
		}
	});
	return data;
};

export const ColorField = {
	markup: createColorField,
	init: (context : FormContext) => {},
	data: getColorData
} as FormField;
