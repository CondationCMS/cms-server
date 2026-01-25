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
import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
import { createForm } from "@cms/modules/form/forms.js";
import { openModal } from "@cms/modules/modal.js";
import { buildValuesFromFields } from "@cms/modules/node.js";
import { getListItemTypes, getPageTemplates } from "@cms/modules/rpc/rpc-manager.js";
import { getContent, getContentNode } from "@cms/modules/rpc/rpc-content.js";
import { getPreviewUrl } from "@cms/modules/preview.utils.js";
const createListField = (options, value = []) => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    const nameField = options.options?.nameField || "name";
    var items = "";
    if (value) {
        items = value.map((item, index) => {
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
    }
    return `
		<div class="mb-3 d-flex flex-column cms-form-field" data-cms-form-field-type="list" name="${options.name}" data-name-field="${nameField}">
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
const handleAddItem = (e, container, context) => {
    e.preventDefault();
    const listGroup = container.querySelector(".list-group");
    if (!listGroup)
        return;
    const itemId = createID();
    const nameField = container.getAttribute('data-name-field') || 'name';
    const newItem = { [nameField]: "New Item" };
    const itemMarkup = `
        <div class="list-group-item d-flex justify-content-between align-items-center"
			data-cms-form-field-type="list"
            data-cms-form-field-item="${itemId}"
            data-cms-form-field-item-data='${JSON.stringify(newItem)}'>
            <span class="object-name flex-grow-1">${newItem[nameField]}</span>
            <button class="btn btn-sm btn-outline-danger ms-2 remove-btn" title="Remove">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    `;
    listGroup.insertAdjacentHTML("beforeend", itemMarkup);
    var itemElement = listGroup.querySelector(`[data-cms-form-field-item="${itemId}"]`);
    if (itemElement) {
        itemElement.addEventListener('dblclick', (e) => handleDoubleClick(e, context));
        const removeBtn = itemElement.querySelector('.remove-btn');
        if (removeBtn) {
            removeBtn.addEventListener('click', () => {
                itemElement.remove();
            });
        }
    }
};
const getItemForm = async (el) => {
    var pageTemplates = (await getPageTemplates({})).result;
    const contentNode = await getContentNode({
        url: getPreviewUrl()
    });
    const getContentResponse = await getContent({
        uri: contentNode.result.uri
    });
    var selected = pageTemplates.filter(pageTemplate => pageTemplate.template === getContentResponse?.result?.meta?.template);
    const listContainer = el.closest("[data-cms-form-field-type='list']");
    const fieldName = listContainer?.getAttribute('name');
    var itemForm = [];
    if (selected.length === 1) {
        itemForm = (fieldName && selected[0].data?.forms[fieldName]) ? selected[0].data.forms[fieldName].fields : [];
    }
    if (!itemForm || itemForm.length === 0) {
        let itemTypes = (await getListItemTypes({})).result;
        var selectedItemType = itemTypes.filter(itemType => itemType.name === fieldName);
        itemForm = (selectedItemType.length === 1) ? selectedItemType[0].data?.form.fields : [];
    }
    return itemForm;
};
const handleDoubleClick = async (event, context) => {
    event.preventDefault();
    const el = event.currentTarget;
    const itemDataString = el.getAttribute('data-cms-form-field-item-data');
    if (itemDataString) {
        const itemData = JSON.parse(itemDataString);
        var itemForm = await getItemForm(el);
        const form = createForm({
            fields: itemForm,
            values: {
                ...buildValuesFromFields(itemForm, itemData)
            }
        });
        openModal({
            title: 'Edit Item',
            fullscreen: true,
            form: form,
            onCancel: (event) => { },
            onOk: async (event) => {
                var updateData = form.getRawData();
                el.setAttribute('data-cms-form-field-item-data', JSON.stringify(updateData));
                const listContainer = el.closest("[data-cms-form-field-type='list']");
                const nameField = listContainer?.getAttribute('data-name-field') || 'name';
                el.querySelector('.object-name').textContent = updateData[nameField];
            }
        });
    }
};
const getData = (context) => {
    var data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='list']").forEach((el) => {
        let value = [];
        el.querySelectorAll("[data-cms-form-field-item]").forEach(itemEl => {
            const itemData = itemEl.getAttribute('data-cms-form-field-item-data');
            if (itemData) {
                value.push(JSON.parse(itemData));
            }
        });
        const fieldName = el.getAttribute('name');
        if (fieldName) {
            data[fieldName] = {
                type: 'list',
                value: value
            };
        }
    });
    console.log("send: " + data);
    return data;
};
const init = (context) => {
    context.formElement.querySelectorAll("[data-cms-form-field-type='list']").forEach(listContainer => {
        listContainer.querySelectorAll("[data-cms-form-field-item]").forEach(field => {
            field.addEventListener('dblclick', (e) => handleDoubleClick(e, context));
            // Remove-Button-Listener setzen
            const removeBtn = field.querySelector('.remove-btn');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    field.remove();
                });
            }
        });
        // Event-Listener für den "Add"-Button hinzufügen
        const addButton = listContainer.querySelector("[data-cms-form-field-item-add-btn]");
        if (addButton) {
            addButton.addEventListener("click", (e) => handleAddItem(e, listContainer, context));
        }
    });
};
export const ListField = {
    markup: createListField,
    init: init,
    data: getData
};
