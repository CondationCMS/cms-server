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
import { createID, getUTCDateTimeFromInput, utcToLocalDateTimeInputValue } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js"
import { FieldOptions, FormContext, FormField } from "@cms/modules/form/forms.js";

export interface DateTimeFieldOptions extends FieldOptions {
	placeholder?: string;
}

const createDateTimeField = (options: DateTimeFieldOptions, value : any = '') => {
	const placeholder = options.placeholder || "";
	const id = createID();
	const key = "field." + options.name

	const title = i18n.t(key, options.title)

	let val = '';

	if (value instanceof Date) {
		val = utcToLocalDateTimeInputValue(value.toISOString());
	} else if (typeof value === 'string' && value.length > 0) {
		val = utcToLocalDateTimeInputValue(value);
	}

	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="datetime">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="datetime-local" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${val}">
		</div>
	`;
};


const getDateTimeData = (context : FormContext) => {
	const data = {};
	
  	context.formElement.querySelectorAll("[data-cms-form-field-type='datetime'] input").forEach((el: HTMLInputElement) => {
		const value = getUTCDateTimeFromInput(el); // "2025-05-31T15:30"
		data[el.name] = {
			type: 'datetime',
			value: value === "" ? null : value
		};
	});
	return data;
};

export const DateTimeField = {
	markup: createDateTimeField,
	init: (context : FormContext) => {},
	data: getDateTimeData
} as FormField;
