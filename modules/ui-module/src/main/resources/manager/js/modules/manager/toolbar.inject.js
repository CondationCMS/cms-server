/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import frameMessenger from "@cms/modules/frameMessenger.js";
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, MOVE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "@cms/modules/manager/toolbar-icons";
const addSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
    var command = {
        type: 'add-sectionEntry',
        payload: {
            section: toolbarDefinition.section,
        }
    };
    frameMessenger.send(window.parent, command);
};
const deleteSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
    var command = {
        type: 'delete-sectionEntry',
        payload: {
            sectionUri: toolbarDefinition.uri
        }
    };
    frameMessenger.send(window.parent, command);
};
const setPublishForSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
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
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
    var command = {
        type: 'edit-sections',
        payload: {
            section: toolbarDefinition.section
        }
    };
    frameMessenger.send(window.parent, command);
};
const editContent = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
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
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar || '{}');
    var command = {
        type: 'edit',
        payload: {
            editor: "form",
            element: "meta",
            form: toolbarDefinition.form ? toolbarDefinition.form : "attributes",
            type: toolbarDefinition.type
        }
    };
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
};
const initDragDrop = (container) => {
    const draggableItems = Array.from(container.querySelectorAll(':scope > .cms-ui-editable-sections'));
    if (draggableItems.length === 0) {
        return;
    }
    let draggedEl = null;
    let placeholder = null;
    draggableItems.forEach((item) => {
        item.setAttribute('draggable', 'false');
        const itemToolbar = item.querySelector('.cms-ui-toolbar');
        if (itemToolbar) {
            const handle = document.createElement('button');
            handle.setAttribute('data-cms-drag-handle', '');
            handle.setAttribute('title', 'Drag to reorder');
            handle.innerHTML = MOVE_ICON;
            handle.style.cursor = 'grab';
            handle.addEventListener('mousedown', () => {
                item.setAttribute('draggable', 'true');
            });
            itemToolbar.appendChild(handle);
        }
        item.addEventListener('dragstart', (e) => {
            draggedEl = item;
            e.dataTransfer?.setData('text/plain', '');
            placeholder = document.createElement('div');
            placeholder.setAttribute('data-cms-drag-placeholder', '');
            const cs = getComputedStyle(item);
            placeholder.style.width = item.offsetWidth + 'px';
            placeholder.style.height = item.offsetHeight + 'px';
            placeholder.style.margin = cs.margin;
            placeholder.style.border = '2px dashed #aaa';
            placeholder.style.boxSizing = 'border-box';
            placeholder.style.opacity = '0.5';
            placeholder.style.flexShrink = cs.flexShrink;
            placeholder.style.flexGrow = cs.flexGrow;
            placeholder.style.flexBasis = cs.flexBasis;
            requestAnimationFrame(() => {
                if (draggedEl && placeholder) {
                    container.insertBefore(placeholder, draggedEl);
                    draggedEl.style.display = 'none';
                }
            });
        });
        item.addEventListener('dragend', () => {
            if (draggedEl) {
                draggedEl.style.display = '';
                draggedEl.setAttribute('draggable', 'false');
            }
            placeholder?.remove();
            placeholder = null;
            draggedEl = null;
        });
    });
    container.addEventListener('dragover', (e) => {
        e.preventDefault();
        if (!draggedEl || !placeholder)
            return;
        const siblings = Array.from(container.querySelectorAll(':scope > .cms-ui-editable-sections')).filter(el => el !== draggedEl);
        if (siblings.length === 0)
            return;
        const centers = siblings.map(el => {
            const r = el.getBoundingClientRect();
            return { el, cx: r.left + r.width / 2, cy: r.top + r.height / 2 };
        });
        // Build n+1 gap points for n siblings.
        // Middle gaps: midpoint between consecutive element centers.
        // Edge gaps: extrapolate from the first/last inter-element direction so
        // the "before first" and "after last" zones are symmetric with the rest.
        const gaps = [];
        if (centers.length === 1) {
            const r = siblings[0].getBoundingClientRect();
            gaps.push({ x: r.left, y: r.top + r.height / 2, before: siblings[0] });
            gaps.push({ x: r.right, y: r.top + r.height / 2, before: null });
        }
        else {
            const dx0 = centers[1].cx - centers[0].cx;
            const dy0 = centers[1].cy - centers[0].cy;
            gaps.push({ x: centers[0].cx - dx0 / 2, y: centers[0].cy - dy0 / 2, before: centers[0].el });
            for (let i = 1; i < centers.length; i++) {
                gaps.push({
                    x: (centers[i - 1].cx + centers[i].cx) / 2,
                    y: (centers[i - 1].cy + centers[i].cy) / 2,
                    before: centers[i].el
                });
            }
            const last = centers.length - 1;
            const dxL = centers[last].cx - centers[last - 1].cx;
            const dyL = centers[last].cy - centers[last - 1].cy;
            gaps.push({ x: centers[last].cx + dxL / 2, y: centers[last].cy + dyL / 2, before: null });
        }
        let bestDist = Infinity;
        let bestBefore = undefined;
        for (const gap of gaps) {
            const dx = e.clientX - gap.x;
            const dy = e.clientY - gap.y;
            const dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                bestBefore = gap.before;
            }
        }
        if (bestBefore === undefined)
            return;
        if (bestBefore === null) {
            container.appendChild(placeholder);
        }
        else {
            container.insertBefore(placeholder, bestBefore);
        }
    });
    container.addEventListener('drop', (e) => {
        e.preventDefault();
        if (!draggedEl || !placeholder)
            return;
        container.insertBefore(draggedEl, placeholder);
        placeholder.remove();
        placeholder = null;
        draggedEl.style.display = '';
        draggedEl.setAttribute('draggable', 'false');
        const items = Array.from(container.querySelectorAll(':scope > .cms-ui-editable-sections'));
        const updates = items.map((el, index) => {
            const toolbarData = el.dataset.cmsToolbar ? JSON.parse(el.dataset.cmsToolbar) : {};
            return {
                uri: toolbarData.uri,
                meta: {
                    'layout.order': {
                        type: 'number',
                        value: index
                    }
                }
            };
        }).filter(u => u.uri);
        draggedEl = null;
        frameMessenger.send(window.parent, {
            type: 'sort-sections',
            payload: { updates }
        });
    });
};
export const initToolbar = (container) => {
    var toolbarDefinition = JSON.parse(container.dataset.cmsToolbar || '{}');
    if (!toolbarDefinition.actions) {
        return;
    }
    if (toolbarDefinition.type === "sectionEntry") {
        container.classList.add("cms-ui-editable-sections");
    }
    else {
        container.classList.add("cms-ui-editable");
    }
    const toolbar = document.createElement('div');
    toolbar.className = 'cms-ui-toolbar';
    if (toolbarDefinition.type === "sectionEntry") {
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
    toolbarDefinition.actions.forEach((action) => {
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
        else if (action === "orderSectionEntries") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'editSections');
            button.innerHTML = SECTION_SORT_ICON;
            button.setAttribute("title", "Order");
            button.addEventListener('click', orderSections);
            toolbar.appendChild(button);
        }
        else if (action === "addSectionEntry") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'addSection');
            button.innerHTML = SECTION_ADD_ICON;
            button.setAttribute("title", "Add");
            button.addEventListener('click', addSection);
            toolbar.appendChild(button);
        }
        else if (action === "deleteSectionEntry") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'deleteSection');
            button.innerHTML = SECTION_DELETE_ICON;
            button.setAttribute("title", "Delete");
            button.addEventListener('click', deleteSection);
            toolbar.appendChild(button);
        }
        else if (action === "dragSectionEntries") {
            // Kein Button — DnD wird nach dem ersten Render-Frame initialisiert,
            // damit alle sectionEntry-Toolbars bereits im DOM sind.
            requestAnimationFrame(() => {
                initDragDrop(container);
            });
        }
    });
    if (toolbarDefinition.type === "sectionEntry") {
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
