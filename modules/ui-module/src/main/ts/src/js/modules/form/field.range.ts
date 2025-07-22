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

export interface RangeFieldOptions {
	name?: string;
	title?: string;
	options?: {
		min?: number;
		max?: number;
		step?: number;
	};
}

const createRangeField = (options: RangeFieldOptions, value : string = '') => {
	const id = createID();
	const key = "field." + options.name
	const min = options.options?.min ?? 0;
	const max = options.options?.max ?? 100;
	const step = options.options?.step ?? 1;
	const title = i18n.t(key, options.title)

	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="range">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}: <span id="${id}-value">${value || min}</span></label>
			<input type="range" class="form-range" id="${id}" name="${options.name}" 
				min="${min}" max="${max}" step="${step}" value="${value || min}" 
				oninput="document.getElementById('${id}-value').textContent = this.value">
		</div>
	`;
};

const getData = () => {
	const data = {};
	document.querySelectorAll("[data-cms-form-field-type='range'] input").forEach((el : HTMLInputElement) => {
		data[el.name] = {
			type: 'range',
			value: parseFloat(el.value)
		};
	});
	return data;
};

export const RangeField = {
	markup: createRangeField,
	init: () => {},
	data: getData
};
