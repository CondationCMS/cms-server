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

let monacoEditors = [];

const createCodeField = (options, value = '') => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	return `
		<div class="mb-3 h-100 cms-form-field" data-cms-form-field-type="code">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			<div id="${id}" class="monaco-editor-container" style="height: ${options.height || '300px'}; border: 1px solid #ccc;"></div>
			<input type="hidden" name="${options.name}" data-monaco-id="${id}" data-initial-value="${encodeURIComponent(value)}">
		</div>
	`;
};

const getData = (context) => {
	var data = {}
	monacoEditors.forEach(({ input, editor }) => {
		data[input.name] = {
			type: 'code',
			value: editor.getValue()
		};
	});
	return data
}

const init = (context) => {
	monacoEditors = []
	require.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs' } });
	require(['vs/editor/editor.main'], function () {
		const editorInputs = document.querySelectorAll('[data-cms-form-field-type="code"] input');
		editorInputs.forEach(input => {
			const editorContainer = document.getElementById(input.dataset.monacoId);
			const initialValue = decodeURIComponent(input.dataset.initialValue || "");
			const editor = monaco.editor.create(editorContainer, {
				value: initialValue,
				language: 'markdown',
				//theme: 'vs-dark',
				automaticLayout: true
			});
			monacoEditors.push({ input, editor });
		});
	});
}

export const CodeField = {
	markup: createCodeField,
	init: init,
	data: getData
}