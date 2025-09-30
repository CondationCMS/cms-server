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
import { createID, getUTCDateFromInput, utcToLocalDateInputValue } from "./utils.js";
import { i18n } from "../localization.js";
const createDateField = (options, value = '') => {
    const placeholder = options.placeholder || "";
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    let val = '';
    if (value instanceof Date) {
        val = utcToLocalDateInputValue(value.toISOString());
    }
    else if (typeof value === 'string' && value.length > 0) {
        val = utcToLocalDateInputValue(value);
    }
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="date">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="date" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${val}">
		</div>
	`;
};
const getDateData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='date'] input").forEach((el) => {
        const value = getUTCDateFromInput(el); // "2025-05-31"
        data[el.name] = {
            type: "date",
            value: value === "" ? null : value
        }; // Format: "YYYY-MM-DD"
    });
    return data;
};
export const DateField = {
    markup: createDateField,
    init: (context) => { },
    data: getDateData
};
