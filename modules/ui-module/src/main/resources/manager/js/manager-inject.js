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

import frameMessenger from './modules/frameMessenger.js';

document.addEventListener("DOMContentLoaded", function () {
	
	if (!isIframe()) {
		return;
	}
	
	frameMessenger.on('init', (payload) => {
		frameMessenger.send(window.parent, {
			type: 'helloFromIframe',
			payload: { response: 'Hallo Parent!' }
		});
	});
	frameMessenger.send(window.parent, {
		type: 'loaded',
		payload: { }
	});

	const containers = document.querySelectorAll('[data-cms-edit]');
	containers.forEach(contentEditing);

	const containers2 = document.querySelectorAll('[data-cms-edit-section]');
	containers2.forEach(contentEditing2);

	// data-cms-edit-sections='true' data-cms-section-name
	const sectionContainers = document.querySelectorAll('[data-cms-edit-sections]');
	sectionContainers.forEach(sectionEdition);
});

const isIframe = () => {
	return typeof window !== 'undefined' && window.self !== window.top;
}

const EDIT_PAGE_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16">
  <path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/>
  <path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5z"/>
</svg>`;

const EDIT_ATTRIBUTES_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-fill" viewBox="0 0 16 16">
  <path d="M12.854.146a.5.5 0 0 0-.707 0L10.5 1.793 14.207 5.5l1.647-1.646a.5.5 0 0 0 0-.708zm.646 6.061L9.793 2.5 3.293 9H3.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.207zm-7.468 7.468A.5.5 0 0 1 6 13.5V13h-.5a.5.5 0 0 1-.5-.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.5-.5V10h-.5a.5.5 0 0 1-.175-.032l-.179.178a.5.5 0 0 0-.11.168l-2 5a.5.5 0 0 0 .65.65l5-2a.5.5 0 0 0 .168-.11z"/>
</svg>
`;

const SECTION_SORT_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-stack" viewBox="0 0 16 16">
  <path d="m14.12 10.163 1.715.858c.22.11.22.424 0 .534L8.267 15.34a.6.6 0 0 1-.534 0L.165 11.555a.299.299 0 0 1 0-.534l1.716-.858 5.317 2.659c.505.252 1.1.252 1.604 0l5.317-2.66zM7.733.063a.6.6 0 0 1 .534 0l7.568 3.784a.3.3 0 0 1 0 .535L8.267 8.165a.6.6 0 0 1-.534 0L.165 4.382a.299.299 0 0 1 0-.535z"/>
  <path d="m14.12 6.576 1.715.858c.22.11.22.424 0 .534l-7.568 3.784a.6.6 0 0 1-.534 0L.165 7.968a.299.299 0 0 1 0-.534l1.716-.858 5.317 2.659c.505.252 1.1.252 1.604 0z"/>
</svg>
`;

const SECTION_ADD_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-plus" viewBox="0 0 16 16">
  <path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4"/>
</svg>
`;

const SECTION_DELETE_ICON = `
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
  <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"/>
  <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"/>
</svg>
`;

const sectionEdition = (container) => {
	container.classList.add("cms-ui-editable-sections");

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-toolbar';

	const editSectionsButton = document.createElement('button');
	editSectionsButton.setAttribute('data-cms-action', 'editSections');
	editSectionsButton.setAttribute("title", 'Order');
	editSectionsButton.innerHTML = SECTION_SORT_ICON;
	editSectionsButton.addEventListener('click', editSections);
	toolbar.appendChild(editSectionsButton);

	if (container.dataset.cmsAddSection) {
		const addSectionButton = document.createElement('button');
		addSectionButton.setAttribute('data-cms-action', 'addSection');
		addSectionButton.setAttribute("title", 'Add');
		addSectionButton.innerHTML = SECTION_ADD_ICON;
		addSectionButton.addEventListener('click', addSection);
		toolbar.appendChild(addSectionButton);
	}

	container.insertBefore(toolbar, container.firstChild);

	container.addEventListener('mouseover', () => {
		toolbar.classList.add('visible');
	});

	container.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});

	toolbar.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});
};

const addSection = (event) => {
	var $editSections = event.target.closest("[data-cms-edit-sections]")
	var sectionName = $editSections.dataset.cmsSectionName
	
	var command = {
		type: 'add-section',
		payload: {
			sectionName: sectionName
		}
	}
	frameMessenger.send(window.parent, command);
}

const deleteSection = (event) => {
	var $editSections = event.target.closest("[data-cms-section-uri]")
	var sectionUri = $editSections.dataset.cmsSectionUri
	
	var command = {
		type: 'delete-section',
		payload: {
			sectionUri: sectionUri
		}
	}
	frameMessenger.send(window.parent, command);
}

const editSections = (event) => {
	// data-cms-edit-sections='true' data-cms-section-name
	var $editSections = event.target.closest("[data-cms-edit-sections]")
	var sectionName = $editSections.dataset.cmsSectionName

	var command = {
		type: 'edit-sections',
		payload: {
			sectionName: sectionName
		}
	}
	frameMessenger.send(window.parent, command);
}

const contentEditing2 = (container) => {
	container.classList.add("cms-ui-editable");

	if (container.querySelector('.cms-ui-section-toolbar'))
		return;

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-section-toolbar';
	
	if (container.dataset.cmsDeleteSection) {
		const deleteSectionButton = document.createElement('button');
		deleteSectionButton.setAttribute('data-cms-action', 'deleteSection');
		deleteSectionButton.setAttribute("title", 'Delete');
		deleteSectionButton.innerHTML = SECTION_DELETE_ICON;
		deleteSectionButton.addEventListener('click', deleteSection);
		toolbar.appendChild(deleteSectionButton);
	}

	container.insertBefore(toolbar, container.firstChild);
	
	container.addEventListener('mouseover', () => {
		toolbar.classList.add('visible');
	});

	container.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});

	toolbar.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});
};

const contentEditing = (container) => {
	container.classList.add("cms-ui-editable");

	if (container.querySelector('.cms-ui-toolbar'))
		return;

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-toolbar';

	const button = document.createElement('button');
	button.setAttribute('data-cms-action', 'edit');
	
	if (container.dataset.cmsElement === "content") {
		//button.textContent = 'Edit Content';
		button.innerHTML = EDIT_PAGE_ICON;
		button.setAttribute("title", "Edit content");
	} else {
		//button.textContent = 'Edit';
		button.innerHTML = EDIT_ATTRIBUTES_ICON;
		button.setAttribute("title", "Edit attributes");
	}
	
	button.addEventListener('click', edit);

	toolbar.appendChild(button);


	if (container.dataset.cmsDeleteSection) {
		const deleteSectionButton = document.createElement('button');
		deleteSectionButton.setAttribute('data-cms-action', 'deleteSection');
		deleteSectionButton.setAttribute("title", 'Delete');
		deleteSectionButton.innerHTML = SECTION_DELETE_ICON;
		deleteSectionButton.addEventListener('click', deleteSection);
		toolbar.appendChild(deleteSectionButton);
	}

	container.insertBefore(toolbar, container.firstChild);
	//const toolbar = container.querySelector('.toolbar');

	container.addEventListener('mouseover', () => {
		toolbar.classList.add('visible');
	});

	container.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});

	toolbar.addEventListener('mouseleave', (event) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
			toolbar.classList.remove('visible');
		}
	});
};

const edit = (event) => {
	var $editor = event.target.closest('[data-cms-editor]');
	if ($editor) {
		var contentUri = event.target.closest('[data-cms-content-uri]')

		var command = {
			type: 'edit',
			payload: {
				editor: $editor.dataset.cmsEditor,
				element: $editor.dataset.cmsElement,
				options: $editor.dataset.cmsEditorOptions ? JSON.parse($editor.dataset.cmsEditorOptions) : {}
			}
		}
		if ($editor.dataset.cmsMetaElement) {
			command.payload.metaElement = $editor.dataset.cmsMetaElement
		}
		if ($editor.dataset.cmsEditor === "form") {
			var elements = []
			$editor.querySelectorAll("[data-cms-editor]").forEach($elem => {
				if ($elem.dataset.cmsElement === "meta") {
					elements.push({
						name: $elem.dataset.cmsMetaElement,
						editor: $elem.dataset.cmsEditor,
						options: $elem.dataset.cmsEditorOptions ? JSON.parse($elem.dataset.cmsEditorOptions) : {}
					})
				}
			})
			command.payload.metaElements = elements
		}

		if (contentUri) {
			command.payload.uri = contentUri.dataset.cmsContentUri
		}

		frameMessenger.send(window.parent, command);
	}
}