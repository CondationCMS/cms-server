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
import { TextField } from "./forms.field.text.js";
import { MailField } from "./forms.field.mail.js";
import { CodeField } from "./forms.field.code.js";
import { SelectField } from "./forms.field.select.js";
import { MarkdownField } from "./forms.field.markdown.js";



const createForm = (options) => {
	console.log("form", options)
	const fields = options.fields || [];
	const values = options.values || {};
	const formId = createID();

	const fieldHtml = fields.map(field => {
		const val = values[field.name] || '';
		switch (field.type) {
			case 'email':
				return MailField.markup(field, val);
			case 'text':
				return TextField.markup(field, val);
			case 'select':
				return SelectField.markup(field, val);
			case 'code':
				return CodeField.markup(field, val);
			case 'markdown':
				return MarkdownField.markup(field, val);
			default:
				return '';
		}
	}).join('\n');

	const html = `
		<form id="${formId}" class="needs-validation" novalidate>
			${fieldHtml}
		</form>
	`;

	let formElement = null;

	const init = (container) => {
		if (typeof container === 'string') {
			container = document.querySelector(container);
		}
		if (!container) {
			console.error("Form-Container nicht gefunden.");
			return;
		}
		container.innerHTML = html;
		formElement = container.querySelector('form');

		formElement.addEventListener('keydown', (e) => {
			if (e.key === 'Enter' && e.target.tagName.toLowerCase() !== 'textarea') {
				e.preventDefault();
			}
		});

		formElement.addEventListener('submit', (e) => {
			e.preventDefault();
			e.stopPropagation();
			formElement.classList.add('was-validated');
		});
		CodeField.init()
		MarkdownField.init()
		/*
		require.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs' } });
		require(['vs/editor/editor.main'], function () {
			const editorInputs = formElement.querySelectorAll('[data-monaco-id]');
			editorInputs.forEach(input => {
				const editorContainer = document.getElementById(input.dataset.monacoId);
				const initialValue = decodeURIComponent(input.dataset.initialValue || "");
				const editor = monaco.editor.create(editorContainer, {
					value: initialValue,
					language: 'markdown',
					theme: 'vs-dark',
					automaticLayout: true
				});
				monacoEditors.push({ input, editor });
			});
		});
		*/
	};

	const getData = () => {
		if (!formElement) {
			console.warn("Formular wurde noch nicht initialisiert.");
			return {};
		}
		const data = {
			...TextField.data(),
			...SelectField.data(),
			...MailField.data(),
			...CodeField.data()
		};
		return data
	};

	return {
		init,
		getData
	};
};

export { createForm };
