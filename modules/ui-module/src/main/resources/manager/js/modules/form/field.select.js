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
import { i18n } from "../localization.js";
const createSelectField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    const optionTags = (options.options?.choices || []).map(opt => {
        const label = typeof opt === 'object' ? opt.label : opt;
        const val = typeof opt === 'object' ? opt.value : opt;
        const selected = val === value ? ' selected' : '';
        return `<option value="${val}"${selected}>${label}</option>`;
    }).join('\n');
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="select">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<select class="form-select" id="${id}" name="${options.name}">
				${optionTags}
			</select>
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement
        .querySelectorAll("[data-cms-form-field-type='select'] select")
        .forEach((el) => {
        let value = el.value;
        // optional: type-konvertierung, aber fallback ist immer der echte Wert
        if (value === 'true') {
            value = true;
        }
        else if (value === 'false') {
            value = false;
        }
        data[el.name] = {
            type: 'select',
            value: value
        };
    });
    return data;
};
export const SelectField = {
    markup: createSelectField,
    init: (context) => { },
    data: getData
};
