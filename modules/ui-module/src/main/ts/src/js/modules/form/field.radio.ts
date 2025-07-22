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

export interface RadioFieldOptions {
	name?: string;
	title?: string;
	options?: {	
		choices: Array<{
			label: string;
			value: string;
		}>;
	};	
}

const createRadioField = (options: RadioFieldOptions, value: string = '') => {
	const id = createID();
	const key = "field." + options.name
	const name = options.name || id;
	const title = i18n.t(key, options.title)
	const choices = options.options?.choices || [];

	const radios = choices.map((choice, idx) => {
		const inputId = `${id}-${idx}`;
		const checked = value === choice.value ? 'checked' : '';
		return `
			<div class="form-check cms-form-field">
				<input class="form-check-input" type="radio" name="${name}" id="${inputId}" value="${choice.value}" ${checked}>
				<label class="form-check-label" for="${inputId}">
					${choice.label}
				</label>
			</div>
		`;
	}).join('');

	return `
		<div class="mb-3" data-cms-form-field-type="radio">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			${radios}
		</div>
	`;
};

const getData = () => {
	const data = {};
	document.querySelectorAll("[data-cms-form-field-type='radio']").forEach(container => {
		const name = (container.querySelector("input[type='radio']") as HTMLInputElement).name;
		const checked = container.querySelector("input[type='radio']:checked") as HTMLInputElement;
		if (checked) {
			data[name] = {
				type: 'radio',
				value: checked.value
			};
		}
	});
	return data;
};

export const RadioField = {
	markup: createRadioField,
	init: () => {},
	data: getData
};
