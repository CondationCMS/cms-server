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
import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js"
import { FieldOptions, Form, FormContext, FormField } from "@cms/modules/form/forms.js";

export interface TextAreaFieldOptions extends FieldOptions {
	rows?: number;
}

const createTextAreaField = (options: TextAreaFieldOptions, value : string = '') => {
	const rows : number = options.rows || 5;
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="text">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<textarea class="form-control" id="${id}" name="${options.name}">${value || ''}</textarea>
		</div>
	`;
};

const getData = (context : FormContext) => {
	var data = {}
	context.formElement.querySelectorAll("[data-cms-form-field-type='text'] textarea").forEach((el : HTMLInputElement) => {
		let value = el.value
		data[el.name] = {
			type: 'textarea',
			value: value
		}
	})
	return data
}

export const TextAreaField = {
	markup: createTextAreaField,
	init: (context : FormContext) => {},
	data : getData
} as FormField