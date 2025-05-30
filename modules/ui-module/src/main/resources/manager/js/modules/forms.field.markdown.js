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
import { createID } from "./forms.utils.js";
//import '/manager/js/libs/cherry-markdown/cherry-markdown.css';
import Cherry from "/manager/js/libs/cherry-markdown/cherry-markdown.core.js"

let cherryEditors = [];

const createMarkdownField = (options, value = '') => {
	const id = createID();
	return `
		<div class="mb-3" data-cms-form-field-type="markdown">
			<label class="form-label">${options.title}</label>
			<div id="${id}" class="cherry-editor-container" style="height: ${options.height || '300px'}; border: 1px solid #ccc;"></div>
			<input type="hidden" name="${options.name}" data-cherry-id="${id}" data-initial-value="${encodeURIComponent(value)}">
		</div>
	`;
};

const getData = () => {
	const data = {};
	cherryEditors.forEach(({ input, editor }) => {
		data[input.name] = editor.getMarkdown();
	});
	return data;
};

const init = () => {
	cherryEditors = [];

	const editorInputs = document.querySelectorAll('[data-cms-form-field-type="markdown"] input');
	editorInputs.forEach(input => {
		const containerId = input.dataset.cherryId;
		const initialValue = decodeURIComponent(input.dataset.initialValue || "");

		const editor = new Cherry({
			id: containerId,
			value: initialValue,
			height: '100%',
			editor: {
				defaultModel: 'edit&preview'
			}
		});

		cherryEditors.push({ input, editor });
	});
};

export const MarkdownField = {
	markup: createMarkdownField,
	init: init,
	data: getData
};
