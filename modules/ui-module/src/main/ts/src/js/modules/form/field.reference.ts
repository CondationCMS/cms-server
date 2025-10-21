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
import { openFileBrowser } from "../filebrowser.js";
import { FieldOptions, FormContext, FormField } from "./forms.js";

export interface ReferenceFieldOptions extends FieldOptions {
}

const createReferenceField = (options: ReferenceFieldOptions, value: string = '') => {
	const id = createID();
	const key = "field." + options.name;
	const title = i18n.t(key, options.title);

	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="reference">
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

const getData = (context: FormContext) => {
	const data = {};

	context.formElement.querySelectorAll("[data-cms-form-field-type='reference'] input").forEach((el: HTMLInputElement) => {
		let value = el.value
		data[el.name] = {
			type: 'reference',
			value: value
		}
	})
	return data;
};

const init = (context: FormContext) => {
	context.formElement.querySelectorAll("[data-cms-form-field-type='reference']").forEach(wrapper => {
		const openMediaManager = wrapper.querySelector(".cms-reference-button") as HTMLButtonElement;

		if (!openMediaManager) return;


		// Handle MediaManager button robust: remove old handler, use onclick
		openMediaManager.onclick = null;
		openMediaManager.onclick = () => {
			openFileBrowser({
				type: "content",
				filter: (file) => {
					return file.content || file.directory;
				},
				onSelect: (file: any) => {
					const inputValue = wrapper.querySelector(".cms-reference-input-value") as HTMLInputElement;
					if (file && file.uri) {
						var value = file.uri; // Use the file's URI
						if (file.uri.startsWith("/")) {
							value = file.uri.substring(1); // Remove leading slash if present
						}
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
} as FormField;
