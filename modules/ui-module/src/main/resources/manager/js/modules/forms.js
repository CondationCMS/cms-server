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
const createID = () => "id" + Math.random().toString(16).slice(2);

const createEmailField = (options, value = '') => {
	const placeholder = options.placeholder || ""
	const id = createID()
	const key = options.key || ""
	return `
		<div class="mb-3">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${options.title}</label>
			<input type="email" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${value || ''}">
		</div>
	`;
};

const createTextField = (options, value = '') => {
	const placeholder = options.placeholder || "";
	const id = createID();
	const key = options.key || ""
	return `
		<div class="mb-3">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${options.title}</label>
			<input type="text" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${value || ''}">
		</div>
	`;
};

const createSelectField = (options, value = '') => {
	const id = createID();
	const key = options.key || ""
	const optionTags = (options.options || []).map(opt => {
		const label = typeof opt === 'object' ? opt.label : opt;
		const val = typeof opt === 'object' ? opt.value : opt;
		const selected = val === value ? ' selected' : '';
		return `<option value="${val}"${selected}>${label}</option>`;
	}).join('\n');

	return `
		<div class="mb-3">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${options.title}</label>
			<select class="form-select" id="${id}" name="${options.name}">
				${optionTags}
			</select>
		</div>
	`;
};

const createCodeField = (options, value = '') => {
	const id = createID();
	const key = options.key || ""
	return `
		<div class="mb-3">
			<label class="form-label">${options.title}</label>
			<div id="${id}" class="monaco-editor-container" style="height: ${options.height || '300px'}; border: 1px solid #ccc;"></div>
			<input type="hidden" name="${options.name}" data-monaco-id="${id}" data-initial-value="${encodeURIComponent(value)}">
		</div>
	`;
};

const createForm = (options) => {
	const fields = options.fields || [];
	const values = options.values || {};
	const formId = createID();

	const fieldHtml = fields.map(field => {
		const val = values[field.name] || '';
		switch (field.type) {
			case 'email':
				return createEmailField(field, val);
			case 'text':
				return createTextField(field, val);
			case 'select':
				return createSelectField(field, val);
			case 'code':
				return createCodeField(field, val);
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

	let monacoEditors = [];

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

		require.config({paths: {vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs'}});
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
				monacoEditors.push({input, editor});
			});
		});
	};

	const getData = () => {
		if (!formElement) {
			console.warn("Formular wurde noch nicht initialisiert.");
			return {};
		}
		const data = {};
		formElement.querySelectorAll('[name]').forEach(el => {
			let value = el.value;

			// Boolean-Konvertierung bei den Strings "true"/"false"
			if (value === 'true') {
				value = true;
			} else if (value === 'false') {
				value = false;
			}

			data[el.name] = value;
			
			monacoEditors.forEach(({ input, editor }) => {
				data[input.name] = editor.getValue();
			});
		});
		return data;
	};

	return {
		init,
		getData
	};
};

export { createForm };
