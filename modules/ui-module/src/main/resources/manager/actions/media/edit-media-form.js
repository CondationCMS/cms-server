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
import { openSidebar } from '../../js/modules/sidebar.js';
import { createForm } from '../../js/modules/form/forms.js';
import { showToast } from '../../js/modules/toast.js';
import { setMeta } from '../../js/modules/rpc/rpc-content.js';
import { reloadPreview } from '../../js/modules/preview.utils.js';
import { i18n } from '../../js/modules/localization.js';
import { getMediaForm } from '../../js/modules/rpc/rpc-manager.js';
import { getMediaMetaData, setMediaMetaData } from '../../js/modules/rpc/rpc-media.js';
export async function runAction(params) {
    var mediaForm = (await getMediaForm({
        form: params.options.form || 'meta'
    })).result;
    const fields = [
        ...mediaForm?.form?.fields
    ];
    const values = {
        ...(await getMediaMetaData({ image: params.options.image })).result.meta
    };
    const form = createForm({
        fields: fields,
        values: values
    });
    openSidebar({
        title: 'Media attributes',
        body: 'modal body',
        form: form,
        onCancel: (event) => { },
        onOk: async (event) => {
            var updateData = form.getData();
            var setMetaResponse = await setMediaMetaData({
                image: params.options.image,
                meta: updateData
            });
            showToast({
                title: i18n.t('manager.actions.media.edit-media-form.toast.title', "Media meta updated"),
                message: i18n.t('manager.actions.media.edit-media-form.toast.message', "The media meta have been updated successfully."),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
            reloadPreview();
        }
    });
}
/**
 * Retrieves a nested value from an object using a dot-notated path like "meta.title"
 * @param {object} sourceObj - The object to retrieve the value from
 * @param {string} path - Dot-notated string path, e.g., "meta.title"
 * @returns {*} - The value found at the given path, or undefined if not found
 */
const getValueByPath = (sourceObj, path) => {
    return path.split('.').reduce((acc, part) => acc?.[part], sourceObj);
};
/**
 * Builds a values object from an array of form fields
 * @param {Array} fields - Array of form field objects, each with a .name property
 * @param {object} sourceObj - The source object to extract the values from
 * @returns {object} values - An object mapping field names to their corresponding values
 */
const buildValuesFromFields = (fields, sourceObj) => {
    const values = {};
    for (const field of fields) {
        if (!field.name)
            continue;
        values[field.name] = getValueByPath(sourceObj, field.name);
    }
    return values;
};
