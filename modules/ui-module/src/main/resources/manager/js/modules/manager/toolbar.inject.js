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
import frameMessenger from "../frameMessenger";
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "./toolbar-icons";
const addSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'add-section',
        payload: {
            sectionName: toolbarDefinition.sectionName,
        }
    };
    frameMessenger.send(window.parent, command);
};
const deleteSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'delete-section',
        payload: {
            sectionUri: toolbarDefinition.uri
        }
    };
    frameMessenger.send(window.parent, command);
};
const setPublishForSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var action = event.currentTarget.getAttribute('data-cms-action');
    var command = {
        type: 'section-set-published',
        payload: {
            sectionUri: toolbarDefinition.uri,
            published: action === "publish"
        }
    };
    frameMessenger.send(window.parent, command);
};
const orderSections = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit-sections',
        payload: {
            sectionName: toolbarDefinition.sectionName
        }
    };
    frameMessenger.send(window.parent, command);
};
const editContent = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit',
        payload: {
            editor: "markdown",
            element: "content"
        }
    };
    if (toolbarDefinition.uri) {
        command.payload.uri = toolbarDefinition.uri;
    }
    frameMessenger.send(window.parent, command);
};
const editAttributes = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit',
        payload: {
            editor: "form",
            element: "meta"
        }
    };
    if (toolbarDefinition.uri) {
        command.payload.uri = toolbarDefinition.uri;
    }
    var elements = [];
    toolbar.parentNode.querySelectorAll("[data-cms-editor]").forEach(($elem) => {
        var toolbar = $elem.dataset.cmsToolbar ? JSON.parse($elem.dataset.cmsToolbar) : {};
        if ($elem.dataset.cmsElement === "meta"
            && (!toolbar.id || toolbar.id === toolbarDefinition.id)) {
            elements.push({
                name: $elem.dataset.cmsMetaElement,
                editor: $elem.dataset.cmsEditor,
                options: $elem.dataset.cmsEditorOptions ? JSON.parse($elem.dataset.cmsEditorOptions) : {}
            });
        }
    });
    command.payload.metaElements = elements;
    frameMessenger.send(window.parent, command);
};
export const initToolbar = (container) => {
    var toolbarDefinition = JSON.parse(container.dataset.cmsToolbar);
    if (!toolbarDefinition.actions) {
        return;
    }
    if (toolbarDefinition.type === "sections") {
        container.classList.add("cms-ui-editable-sections");
    }
    else {
        container.classList.add("cms-ui-editable");
    }
    const toolbar = document.createElement('div');
    toolbar.className = 'cms-ui-toolbar';
    if (toolbarDefinition.type === "sections") {
        toolbar.classList.add("cms-ui-toolbar-tl");
    }
    else {
        toolbar.classList.add("cms-ui-toolbar-tr");
    }
    toolbar.classList.add("cms-ui-toolbar");
    toolbar.addEventListener('mouseover', () => {
        toolbar.classList.add('visible');
    });
    toolbar.addEventListener('mouseleave', (event) => {
        if (!event.relatedTarget || !toolbar.contains(event.relatedTarget)) {
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
        }
        else if (action === "editAttributes") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'editAttributes');
            button.innerHTML = EDIT_ATTRIBUTES_ICON;
            button.setAttribute("title", "Edit attributes");
            button.addEventListener('click', editAttributes);
            toolbar.appendChild(button);
        }
        else if (action === "orderSections") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'editSections');
            button.innerHTML = SECTION_SORT_ICON;
            button.setAttribute("title", "Order");
            button.addEventListener('click', orderSections);
            toolbar.appendChild(button);
        }
        else if (action === "addSection") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'addSection');
            button.innerHTML = SECTION_ADD_ICON;
            button.setAttribute("title", "Add");
            button.addEventListener('click', addSection);
            toolbar.appendChild(button);
        }
        else if (action === "deleteSection") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'deleteSection');
            button.innerHTML = SECTION_DELETE_ICON;
            button.setAttribute("title", "Delete");
            button.addEventListener('click', deleteSection);
            toolbar.appendChild(button);
        }
    });
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
