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
import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
import { createTaxonomyValue, getTaxonomyValues } from "@cms/modules/rpc/rpc-taxonomy.js";
const createTagsField = (options, value = []) => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    const slug = options.options?.taxonomy || '';
    const valueArray = Array.isArray(value) ? value : (value ? [value] : []);
    const selectedJson = JSON.stringify(valueArray);
    return `
        <div class="mb-3 cms-form-field" data-cms-form-field-type="tags" data-taxonomy-slug="${slug}" data-selected-values="${selectedJson.replace(/"/g, '&quot;')}">
            <label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
            <select id="${id}" name="${options.name}" class="form-select" multiple data-allow-clear="true" data-allow-new="true"></select>
        </div>
    `;
};
const init = (context) => {
    if (!context.formElement) {
        return;
    }
    context.formElement.querySelectorAll("[data-cms-form-field-type='tags']").forEach(async (wrapper) => {
        const slug = wrapper.dataset.taxonomySlug || '';
        const selectedRaw = wrapper.dataset.selectedValues || '[]';
        const selected = JSON.parse(selectedRaw);
        const select = wrapper.querySelector('select');
        if (!select || !slug) {
            return;
        }
        try {
            const values = await getTaxonomyValues(slug);
            values.forEach(val => {
                const option = document.createElement('option');
                option.value = val.id;
                option.text = val.title;
                option.selected = selected.includes(val.id);
                select.appendChild(option);
            });
            selected
                .filter(value => !values.some(taxonomyValue => taxonomyValue.id === value))
                .forEach(value => {
                const option = document.createElement('option');
                option.value = value;
                option.text = value;
                option.selected = true;
                select.appendChild(option);
            });
            if (typeof Tags !== 'undefined') {
                Tags.init(`#${select.id}`, {
                    allowNew: true,
                    onCreateItem: (option) => {
                        const title = getTagTitle(option);
                        if (!title) {
                            return;
                        }
                        createTaxonomyValue(slug, title)
                            .then(created => {
                            option.value = created.id;
                            option.text = created.title;
                            option.label = created.title;
                            option.selected = true;
                        })
                            .catch(e => console.error('Failed to create taxonomy value:', slug, title, e));
                    }
                });
            }
            else {
                console.error('bootstrap5-tags not loaded - falling back to native multi-select');
            }
        }
        catch (e) {
            console.error('Failed to load taxonomy values for slug:', slug, e);
            const errorMsg = document.createElement('div');
            errorMsg.className = 'text-danger small mt-1';
            errorMsg.textContent = 'Could not load taxonomy values.';
            wrapper.appendChild(errorMsg);
        }
    });
};
const getTagTitle = (item) => {
    if (typeof item === 'string') {
        return item.trim();
    }
    if (item instanceof HTMLOptionElement) {
        return String(item.text || item.label || item.value || '').trim();
    }
    return String(item?.label || item?.text || item?.value || '').trim();
};
const getData = (context) => {
    const data = {};
    if (!context.formElement) {
        return data;
    }
    context.formElement.querySelectorAll("[data-cms-form-field-type='tags'] select").forEach((el) => {
        const select = el;
        const values = Array.from(select.selectedOptions).map(opt => opt.value);
        data[select.name] = {
            type: 'tags',
            value: values
        };
    });
    return data;
};
export const TagsField = {
    markup: createTagsField,
    init: init,
    data: getData
};
