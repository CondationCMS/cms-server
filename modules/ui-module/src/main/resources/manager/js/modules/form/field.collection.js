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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import { openCollectionItemPicker } from '@cms/modules/collection-picker.js';
import { createID } from '@cms/modules/form/utils.js';
import { i18n } from '@cms/modules/localization.js';
const escapeHtml = (value) => String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
const createCollectionField = (options, value = '') => {
    const id = createID();
    const key = `field.${options.name}`;
    const title = i18n.t(key, options.title);
    const collection = options.options?.collection || '';
    const disabled = collection ? '' : ' disabled';
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="collection"
			data-cms-collection="${escapeHtml(collection)}">
			<label for="${id}" class="form-label" cms-i18n-key="${escapeHtml(key)}">${escapeHtml(title)}</label>
			<div class="input-group">
				<input type="text" class="form-control cms-collection-input-value" id="${id}"
					name="${escapeHtml(options.name)}" value="${escapeHtml(value)}" readonly>
				<button type="button" class="btn btn-outline-primary cms-collection-select"${disabled}>
					<i class="bi bi-list-ul me-1"></i>${escapeHtml(i18n.t('collection.field.select', 'Select'))}
				</button>
				<button type="button" class="btn btn-outline-secondary cms-collection-clear"
					aria-label="${escapeHtml(i18n.t('collection.field.clear', 'Clear selection'))}">
					<i class="bi bi-x-lg"></i>
				</button>
			</div>
			${collection ? '' : `<div class="form-text text-danger">${escapeHtml(i18n.t('collection.field.missingCollection', 'No collection is configured for this field.'))}</div>`}
		</div>`;
};
const init = (context) => {
    const formElement = context.formElement;
    if (!formElement)
        return;
    formElement.querySelectorAll("[data-cms-form-field-type='collection']").forEach(wrapper => {
        const input = wrapper.querySelector('.cms-collection-input-value');
        const selectButton = wrapper.querySelector('.cms-collection-select');
        const clearButton = wrapper.querySelector('.cms-collection-clear');
        const collection = wrapper.dataset.cmsCollection;
        if (!input || !selectButton || !clearButton || !collection)
            return;
        selectButton.addEventListener('click', () => {
            openCollectionItemPicker({
                collection,
                title: i18n.t('collection.field.dialogTitle', `Select item from ${collection}`),
                onSelect: item => {
                    input.value = item.id;
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }
            });
        });
        clearButton.addEventListener('click', () => {
            input.value = '';
            input.dispatchEvent(new Event('change', { bubbles: true }));
        });
    });
};
const getData = (context) => {
    const data = {};
    context.formElement?.querySelectorAll("[data-cms-form-field-type='collection'] input[name]").forEach(input => {
        data[input.name] = {
            type: 'collection',
            value: input.value
        };
    });
    return data;
};
export const CollectionField = {
    markup: createCollectionField,
    init,
    data: getData
};
