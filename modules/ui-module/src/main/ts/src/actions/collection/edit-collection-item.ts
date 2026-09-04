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

import { createForm, Form, getFormFields } from '@cms/modules/form/forms.js';
import { i18n } from '@cms/modules/localization.js';
import { openModal } from '@cms/modules/modal.js';
import { buildValuesFromFields } from '@cms/modules/node.js';
import { reloadPreview } from '@cms/modules/preview.utils.js';
import { getCollectionItem, saveCollectionItem } from '@cms/modules/rpc/rpc-collection.js';
import { CollectionType, getCollectionTypes } from '@cms/modules/rpc/rpc-manager.js';
import { showToast } from '@cms/modules/toast.js';

const CONTENT_FIELD = 'content';

const defaultForm = {
	fields: [
		{ type: 'text', name: 'title', title: 'Title', required: true },
		{ type: 'markdown', name: CONTENT_FIELD, title: 'Content', height: '60vh' }
	],
	tabs: []
};

export const collectionForm = (
	types: CollectionType[],
	collection: string,
	mode: 'create' | 'edit' = 'edit'
): any => {
	const forms = types.find(type => type.name === collection)?.forms;
	return forms?.[mode] ?? forms?.edit ?? defaultForm;
};

export interface EditCollectionItemOptions {
	collection: string;
	id: string;
	reloadAfterSave?: boolean;
	onSaved?: () => void | Promise<void>;
}

export interface CollectionItemEditor {
	form: Form;
	save: () => Promise<boolean>;
}

export const createCollectionItemEditor = async (
	options: EditCollectionItemOptions
): Promise<CollectionItemEditor> => {
	const [item, typeResponse] = await Promise.all([
		getCollectionItem(options.collection, options.id),
		getCollectionTypes()
	]);
	const definition = collectionForm(typeResponse.result, options.collection);
	const fields = getFormFields(definition);
	const form = createForm({
		fields: definition.fields ?? [],
		tabs: definition.tabs ?? [],
		values: {
			...buildValuesFromFields(fields, item.meta),
			[CONTENT_FIELD]: item.content
		}
	});

	return {
		form,
		save: async () => {
			const data = form.getData();
			const content = data[CONTENT_FIELD];
			delete data[CONTENT_FIELD];
			try {
				await saveCollectionItem({
					collection: options.collection,
					id: options.id,
					content,
					meta: data
				});
				showToast({
					title: i18n.t('collection.item.edit.success.title', 'Collection item updated'),
					message: i18n.t('collection.item.edit.success.message', 'The collection item was updated successfully.'),
					type: 'success',
					timeout: 3000
				});
				await options.onSaved?.();
				if (options.reloadAfterSave) {
					reloadPreview();
				}
				return true;
			} catch (error: any) {
				showToast({
					title: i18n.t('collection.item.edit.error.title', 'Collection item not updated'),
					message: error?.message ?? String(error),
					type: 'error',
					timeout: 3000
				});
				return false;
			}
		}
	};
};

export const openCollectionItemEditor = async (options: EditCollectionItemOptions) => {
	try {
		const editor = await createCollectionItemEditor(options);

		openModal({
			title: i18n.t('collection.item.edit.title', 'Edit collection item'),
			body: '',
			form: editor.form,
			fullscreen: true,
			onCancel: () => {},
			onOk: editor.save
		});
	} catch (error: any) {
		showToast({
			title: i18n.t('collection.item.edit.loadError.title', 'Collection item could not be loaded'),
			message: error?.message ?? String(error),
			type: 'error',
			timeout: 3000
		});
	}
};

export const runAction = async (options: EditCollectionItemOptions) => {
	await openCollectionItemEditor({ ...options, reloadAfterSave: true });
};
