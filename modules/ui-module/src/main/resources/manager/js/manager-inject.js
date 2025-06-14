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

import frameMessenger from '/manager/js/modules/frameMessenger.js';

document.addEventListener("DOMContentLoaded", function () {
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

	// data-cms-edit-sections='true' data-cms-section-name
	const sectionContainers = document.querySelectorAll('[data-cms-edit-sections]');
	sectionContainers.forEach(sectionEdition);
});

const sectionEdition = (container) => {
	container.classList.add("cms-ui-editable-sections");

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-toolbar';

	const editSectionsButton = document.createElement('button');
	editSectionsButton.setAttribute('data-cms-action', 'editSections');
	editSectionsButton.textContent = 'Order';
	editSectionsButton.addEventListener('click', editSections);
	toolbar.appendChild(editSectionsButton);

	if (container.dataset.cmsAddSection) {
		const addSectionButton = document.createElement('button');
		addSectionButton.setAttribute('data-cms-action', 'addSection');
		addSectionButton.textContent = 'Add';
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

const contentEditing = (container) => {
	container.classList.add("cms-ui-editable");

	if (container.querySelector('.cms-ui-toolbar'))
		return;

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-toolbar';

	const button = document.createElement('button');
	button.setAttribute('data-cms-action', 'edit');
	
	if (container.dataset.cmsElement === "content") {
		button.textContent = 'Edit Content';
	} else {
		button.textContent = 'Edit';
	}
	
	button.addEventListener('click', edit);

	toolbar.appendChild(button);

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