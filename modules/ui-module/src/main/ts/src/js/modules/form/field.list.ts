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

export interface ListFieldOptions {
	name?: string;
	title?: string;
	nameField?: string;
}

const createListField = (options: ListFieldOptions, value: Array<any> = []) => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	const nameField = options.nameField || "name";
	
	var items = value.map((item, index) => {
		const itemId = createID();
		return `
			<div class="list-group-item d-flex justify-content-between align-items-center"
				data-cms-form-field-item="${itemId}"
				data-cms-form-field-item-data='${JSON.stringify(item)}'>
				<span class="object-name flex-grow-1">${item[nameField]}</span>
				<button class="btn btn-sm btn-outline-danger ms-2 remove-btn" title="Entfernen">
					<i class="bi bi-x-lg"></i>
				</button>
			</div>
		`;
	}).join('\n');
	

	return `
		<div class="mb-3 d-flex flex-column cms-form-field" data-cms-form-field-type="list" name="${options.name}">
			<label class="form-label" for="${id}" cms-i18n-key="${key}">${title}</label>
			<div class="list-group overflow-auto" id="object-list" style="max-height: 200px;">
				${items}
			</div>
			<button class="btn btn-outline-primary btn-sm mt-2"
			data-cms-form-field-item-add-btn>
				+ Add
			</button>
		</div>
	`;
};

const handleAddItem = (container: HTMLElement) => {
	const listGroup = container.querySelector(".list-group");
    if (!listGroup) return;

    const itemId = createID();
    const newItem = {
        name: "New Item" // Standardwert für ein neues Element
    };

    const itemMarkup = `
        <div class="list-group-item d-flex justify-content-between align-items-center"
            data-cms-form-field-item="${itemId}"
            data-cms-form-field-item-data="${JSON.stringify(newItem).replace(/"/g, '&quot;')}">
            <span class="object-name flex-grow-1">${newItem.name}</span>
            <button class="btn btn-sm btn-outline-danger ms-2 remove-btn" title="Entfernen">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    `;

    listGroup.insertAdjacentHTML("beforeend", itemMarkup);

    // Optional: Event-Listener für den neuen "Entfernen"-Button hinzufügen
    const removeBtn = listGroup.querySelector(`[data-cms-form-field-item="${itemId}"] .remove-btn`);
    if (removeBtn) {
        removeBtn.addEventListener("click", () => {
            removeBtn.parentElement?.remove();
        });
    }
}

const handleDoubleClick = (event : Event) => {
	const el = event.currentTarget as HTMLElement;
	const itemData = el.getAttribute('data-cms-form-field-item-data');
	if (itemData) {
		const data = JSON.parse(itemData);
		console.log("Edit item:", data);
	}
}

const getData = () => {
	var data = {}
	document.querySelectorAll("[data-cms-form-field-type='list']").forEach((el: HTMLInputElement) => {
		let value = []
		el.querySelectorAll("[data-cms-form-field-item]").forEach(itemEl => {
			const itemData = itemEl.getAttribute('data-cms-form-field-item-data');
			if (itemData) {
				value.push(JSON.parse(itemData));
			}
		})
		const fieldName = el.getAttribute('name');
        if (fieldName) {
            data[fieldName] = {
                type: 'list',
                value: value
            };
        }
	})
	console.log("send: " + data)
	return data
}

export const ListField = {
	markup: createListField,
	init: () => {
		document.querySelectorAll("[data-cms-form-field-type='list']").forEach(listContainer => {
			
			listContainer.querySelectorAll("[data-cms-form-field-item]").forEach(field => {
				field.addEventListener('dblclick', handleDoubleClick)
			})
		
			// Event-Listener für den "Add"-Button hinzufügen
			const addButton = listContainer.querySelector("[data-cms-form-field-item-add-btn]");
			if (addButton) {
				addButton.addEventListener("click", () => handleAddItem(listContainer as HTMLElement));
			}
		})
	 },
	data: getData
}