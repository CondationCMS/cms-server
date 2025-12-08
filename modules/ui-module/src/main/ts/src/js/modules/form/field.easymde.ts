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
import { FieldOptions, FormContext, FormField } from "@cms/modules/form/forms.js";

let markdownEditors = [];

export interface EasyMDEFieldOptions extends FieldOptions {
}

const createMarkdownField = (options : EasyMDEFieldOptions, value : string = '') => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="easymde">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			<textarea id="${id}" style="display: none; height:0;" data-initial-value="${encodeURIComponent(value)}" name="${options.name}"></textarea>
		</div>
	`;
};

const getData = (context : FormContext) => {
	const data = {};
	markdownEditors.forEach(({ input, editor }) => {
		data[input.name] = {
			type: "easymde",
			value: editor.value()
		}
	});
	return data;
};

const init = (context : FormContext) => {
	markdownEditors = [];

	const editorInputs = document.querySelectorAll('[data-cms-form-field-type="easymde"] textarea');
	editorInputs.forEach((input: HTMLTextAreaElement) => {
		const initialValue = decodeURIComponent(input.dataset.initialValue || "");

		input.value = initialValue; // Set initial value for EasyMDE

		const editor = new window.EasyMDE({
			element: input,
			initialValue: initialValue,
			autoDownloadFontAwesome: true,
			spellChecker: false,
			forceSync: true, // keeps textarea value updated
			toolbar: ["bold", "italic", "heading", "|", "quote", "unordered-list", "ordered-list"]
		});
		
		setTimeout(() => editor.codemirror.refresh(), 500);

		markdownEditors.push({ input, editor });
	});
};

export const EasyMDEField = {
	markup: createMarkdownField,
	init: init,
	data: getData
} as FormField;
