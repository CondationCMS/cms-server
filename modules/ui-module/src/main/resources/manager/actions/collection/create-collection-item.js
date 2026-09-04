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
import { collectionForm } from './edit-collection-item.js';
import { createForm, getFormFields } from '@cms/modules/form/forms.js';
import { i18n } from '@cms/modules/localization.js';
import { openModal } from '@cms/modules/modal.js';
import { loadPreview } from '@cms/modules/preview.utils.js';
import { createCollectionItem } from '@cms/modules/rpc/rpc-collection.js';
import { getCollectionTypes } from '@cms/modules/rpc/rpc-manager.js';
import { showToast } from '@cms/modules/toast.js';
const ID_FIELD = 'id';
const CONTENT_FIELD = 'content';
const fieldValue = (field) => String(field?.value ?? field ?? '').trim();
export const openCollectionItemCreator = async (options) => {
    try {
        const typeResponse = await getCollectionTypes();
        const definition = collectionForm(typeResponse.result, options.collection, 'create');
        const hasIdField = getFormFields(definition).some(field => field.name === ID_FIELD);
        const form = createForm({
            fields: [
                ...(hasIdField ? [] : [{
                        type: 'text',
                        name: ID_FIELD,
                        title: i18n.t('collection.item.create.id', 'Item ID'),
                        required: true
                    }]),
                ...(definition.fields ?? [])
            ],
            tabs: definition.tabs ?? [],
            values: {}
        });
        openModal({
            title: i18n.t('collection.item.create.title', 'Create collection item'),
            body: '',
            form,
            fullscreen: true,
            onCancel: () => { },
            onOk: async () => {
                const data = form.getData();
                const id = fieldValue(data[ID_FIELD]);
                const content = data[CONTENT_FIELD];
                delete data[ID_FIELD];
                delete data[CONTENT_FIELD];
                try {
                    const item = await createCollectionItem({
                        collection: options.collection,
                        id,
                        content,
                        meta: data
                    });
                    showToast({
                        title: i18n.t('collection.item.create.success.title', 'Collection item created'),
                        message: i18n.t('collection.item.create.success.message', 'The collection item was created successfully.'),
                        type: 'success',
                        timeout: 3000
                    });
                    await options.onCreated?.(item);
                    return true;
                }
                catch (error) {
                    showToast({
                        title: i18n.t('collection.item.create.error.title', 'Collection item not created'),
                        message: error?.message ?? String(error),
                        type: 'error',
                        timeout: 3000
                    });
                    return false;
                }
            }
        });
    }
    catch (error) {
        showToast({
            title: i18n.t('collection.item.create.loadError.title', 'Collection form could not be loaded'),
            message: error?.message ?? String(error),
            type: 'error',
            timeout: 3000
        });
    }
};
export const runAction = async (options) => {
    await openCollectionItemCreator({
        ...options,
        onCreated: item => {
            if (item.detailUrl) {
                loadPreview(item.detailUrl);
            }
        }
    });
};
