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
import { openFileBrowser } from "../filebrowser.js";
const createReferenceField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="reference" data-cms-field-options='${JSON.stringify(options || {})}'>
			<div class="d-flex flex-column">
				<div>
					<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
					<input type="text" class="form-control cms-reference-input-value" id="${id}" name="${options.name}" value="${value || ''}">
				</div>
				<button type="button" class="btn btn-outline-primary mt-2 cms-reference-button">
					<i class="bi bi-images me-1"></i> ContentManager
				</button>
			</div>			
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='reference'] input").forEach((el) => {
        let value = el.value;
        data[el.name] = {
            type: 'reference',
            value: value
        };
    });
    return data;
};
const init = (context) => {
    context.formElement.querySelectorAll("[data-cms-form-field-type='reference']").forEach(wrapper => {
        const fileManager = wrapper.querySelector(".cms-reference-button");
        if (!fileManager)
            return;
        let parsedField = null;
        const raw = wrapper.dataset.cmsFieldOptions;
        if (raw) {
            try {
                parsedField = JSON.parse(raw);
            }
            catch (e) {
                console.warn("Invalid field options JSON", e);
            }
        }
        const siteid = parsedField?.options?.siteid ?? undefined;
        // Handle MediaManager button robust: remove old handler, use onclick
        fileManager.onclick = null;
        fileManager.onclick = () => {
            openFileBrowser({
                type: "content",
                siteid: siteid,
                filter: (file) => {
                    return file.content || file.directory;
                },
                onSelect: (file) => {
                    const inputValue = wrapper.querySelector(".cms-reference-input-value");
                    if (file && file.uri) {
                        var value = file.uri; // Use the file's URI
                        inputValue.value = value; // Set the input value to the selected file's name
                    }
                }
            });
        };
    });
};
export const ReferenceField = {
    markup: createReferenceField,
    init: init,
    data: getData
};
