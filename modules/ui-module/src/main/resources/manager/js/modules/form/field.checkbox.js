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
const createCheckboxField = (options, value = []) => {
    const id = createID();
    const key = options.key || "";
    const name = options.name || id;
    const title = options.title || "";
    const choices = options.options.choices || [];
    const selectedValues = new Set(value);
    const checkboxes = choices.map((choice, idx) => {
        const inputId = `${id}-${idx}`;
        const checked = selectedValues.has(choice.value) ? 'checked' : '';
        return `
			<div class="form-check cms-form-field">
				<input class="form-check-input" type="checkbox" name="${name}" id="${inputId}" value="${choice.value}" ${checked}>
				<label class="form-check-label" for="${inputId}">
					${choice.label}
				</label>
			</div>
		`;
    }).join('');
    return `
		<div class="mb-3" data-cms-form-field-type="checkbox">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			${checkboxes}
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='checkbox']").forEach(container => {
        const name = container.querySelector("input[type='checkbox']").name;
        const checkedBoxes = container.querySelectorAll("input[type='checkbox']:checked");
        const values = Array.from(checkedBoxes).map((el) => el.value);
        data[name] = {
            type: 'checkbox',
            value: values
        };
    });
    return data;
};
export const CheckboxField = {
    markup: createCheckboxField,
    init: (context) => { },
    data: getData
};
