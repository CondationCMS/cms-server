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

const createEmailField = (options, value = '') => {
	const placeholder = options.placeholder || ""
	const id = createID()
	const key = options.key || ""
	return `
		<div class="mb-3" data-cms-form-field-type="mail">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${options.title}</label>
			<input type="email" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${value || ''}">
		</div>
	`;
};

const getData = () => {
	var data = {}
	document.querySelectorAll("[data-cms-form-field-type='mail'] input").forEach(el => {
		let value = el.value
		data[el.name] = {
			type: 'mail',
			value: value
		}
	})
	return data
}

export const MailField = {
	markup: createEmailField,
	init: () => {},
	data : getData
}