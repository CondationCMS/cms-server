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

const createEmailField = (options) => {
	const placeholder = options.placeholder || "";
	const id = createID();
	return `
		<div class="mb-3">
			<label for="${id}" class="form-label">${options.title}</label>
			<input type="email" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}">
		</div>
	`;
};

const createTextField = (options) => {
	const placeholder = options.placeholder || "";
	const id = createID();
	return `
		<div class="mb-3">
			<label for="${id}" class="form-label">${options.title}</label>
			<input type="text" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}">
		</div>
	`;
};

const createForm = (options) => {
	const fields = options.fields || [];
	const formId = createID();

	const fieldHtml = fields.map(field => {
		switch (field.type) {
			case 'email': return createEmailField(field);
			case 'text': return createTextField(field);
			default: return '';
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

		// Enter unterdrücken
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
	};

	const getData = () => {
		if (!formElement) {
			console.warn("Formular wurde noch nicht initialisiert.");
			return {};
		}
		const data = {};
		formElement.querySelectorAll('[name]').forEach(el => {
			data[el.name] = el.value;
		});
		return data;
	};

	return {
		init,
		getData
	};
};

export { createForm };
