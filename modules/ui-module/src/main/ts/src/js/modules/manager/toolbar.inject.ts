import frameMessenger from "@cms/modules/frameMessenger.js";
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "@cms/modules/manager/toolbar-icons";

const addSection = (event : Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var command : any = {
		type: 'add-section',
		payload: {
			sectionName: toolbarDefinition.sectionName,
		}
	}
	frameMessenger.send(window.parent, command);
}

const deleteSection = (event: Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var command = {
		type: 'delete-section',
		payload: {
			sectionUri: toolbarDefinition.uri
		}
	}
	frameMessenger.send(window.parent, command);
}

const setPublishForSection = (event: Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var action = (event.currentTarget as HTMLElement).getAttribute('data-cms-action');

	var command = {
		type: 'section-set-published',
		payload: {
			sectionUri: toolbarDefinition.uri,
			published: action === "publish"
		}
	}
	frameMessenger.send(window.parent, command);
}

const orderSections = (event : Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var command = {
		type: 'edit-sections',
		payload: {
			sectionName: toolbarDefinition.sectionName
		}
	}
	frameMessenger.send(window.parent, command);
}


const editContent = (event: Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var command : any = {
		type: 'edit',
		payload: {
			editor: "markdown",
			element: "content"
		}
	}
	if (toolbarDefinition.uri) {
		command.payload.uri = toolbarDefinition.uri;
	}

	frameMessenger.send(window.parent, command);
}

const editAttributes = (event: Event) => {
	var toolbar = (event.target as HTMLElement).closest('[data-cms-toolbar]') as HTMLElement;
	var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar)

	var command : any = {
		type: 'edit',
		payload: {
			editor: "form",
			element: "meta",
			form: toolbarDefinition.form ? toolbarDefinition.form : "attributes",
			type: toolbarDefinition.type
		}
	}
	if (toolbarDefinition.uri) {
		command.payload.uri = toolbarDefinition.uri;
	}
	// legay old style to collect all meta elements for the form editor
	/*
	var elements = []
	toolbar.parentNode.querySelectorAll("[data-cms-editor]").forEach(($elem : HTMLElement) => {
		var toolbar = $elem.dataset.cmsToolbar ? JSON.parse($elem.dataset.cmsToolbar) : {};
		if ($elem.dataset.cmsElement === "meta"
			&& (!toolbar.id || toolbar.id === toolbarDefinition.id)
		) {
			elements.push({
				name: $elem.dataset.cmsMetaElement,
				editor: $elem.dataset.cmsEditor,
				options: $elem.dataset.cmsEditorOptions ? JSON.parse($elem.dataset.cmsEditorOptions) : {}
			})
		}
	})
	command.payload.metaElements = elements
	*/

	frameMessenger.send(window.parent, command);
}


export const initToolbar = (container: HTMLElement) => {

	var toolbarDefinition = JSON.parse(container.dataset.cmsToolbar)
	if (!toolbarDefinition.actions) {
		return
	}
	if (toolbarDefinition.type === "sections") {
		container.classList.add("cms-ui-editable-sections");
	} else {
		container.classList.add("cms-ui-editable");
	}

	const toolbar = document.createElement('div');
	toolbar.className = 'cms-ui-toolbar';

	if (toolbarDefinition.type === "sections") {
		toolbar.classList.add("cms-ui-toolbar-tl");
	} else {
		toolbar.classList.add("cms-ui-toolbar-tr");
	}

	toolbar.classList.add("cms-ui-toolbar");
	toolbar.addEventListener('mouseover', () => {
		toolbar.classList.add('visible');
	});
	toolbar.addEventListener('mouseleave', (event : MouseEvent) => {
		if (!event.relatedTarget || !toolbar.contains(event.relatedTarget as Node)) {
			toolbar.classList.remove('visible');
		}
	});

	toolbarDefinition.actions.forEach(action => {
		if (action === "editContent") {
			const button = document.createElement('button');
			button.setAttribute('data-cms-action', 'edit');
			button.innerHTML = EDIT_PAGE_ICON;
			button.setAttribute("title", "Edit content");
			button.addEventListener('click', editContent);

			toolbar.appendChild(button);
		} else if (action === "editAttributes") {
			const button = document.createElement('button');
			button.setAttribute('data-cms-action', 'editAttributes');
			button.innerHTML = EDIT_ATTRIBUTES_ICON;
			button.setAttribute("title", "Edit attributes");
			button.addEventListener('click', editAttributes);

			toolbar.appendChild(button);
		} else if (action === "orderSections") {
			const button = document.createElement('button');
			button.setAttribute('data-cms-action', 'editSections');
			button.innerHTML = SECTION_SORT_ICON;
			button.setAttribute("title", "Order");
			button.addEventListener('click', orderSections);

			toolbar.appendChild(button);
		} else if (action === "addSection") {
			const button = document.createElement('button');
			button.setAttribute('data-cms-action', 'addSection');
			button.innerHTML = SECTION_ADD_ICON;
			button.setAttribute("title", "Add");
			button.addEventListener('click', addSection);

			toolbar.appendChild(button);
		} else if (action === "deleteSection") {
			const button = document.createElement('button');
			button.setAttribute('data-cms-action', 'deleteSection');
			button.innerHTML = SECTION_DELETE_ICON;
			button.setAttribute("title", "Delete");
			button.addEventListener('click', deleteSection);

			toolbar.appendChild(button);
		}
	})

	if (toolbarDefinition.type === "section") {
		const button = document.createElement('button');
		button.setAttribute('data-cms-action', 'publish');
		button.setAttribute('data-cms-section-uri', toolbarDefinition.uri);
		button.classList.add('cms-unpublished');
		button.innerHTML = SECTION_UNPUBLISHED_ICON;
		button.setAttribute("title", "Publish");
		button.addEventListener('click', setPublishForSection);

		toolbar.appendChild(button);
	}

	container.insertBefore(toolbar, container.firstChild);

	container.addEventListener('mouseover', () => {
		toolbar.classList.add('visible');
	});

	container.addEventListener('mouseleave', (event: MouseEvent) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget as Node)) {
			toolbar.classList.remove('visible');
		}
	});

	toolbar.addEventListener('mouseleave', (event: MouseEvent) => {
		if (!event.relatedTarget || !container.contains(event.relatedTarget as Node)) {
			toolbar.classList.remove('visible');
		}
	});
}