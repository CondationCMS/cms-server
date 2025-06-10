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
import { openSidebar } from '/manager/js/modules/sidebar.js'
import { createForm } from '/manager/js/modules/form/forms.js'
import { showToast } from '/manager/js/modules/toast.js'
import { getContentNode, setMeta, getContent } from '/manager/js/modules/rpc/rpc-content.js'
import { getPreviewUrl, reloadPreview } from '/manager/js/modules/preview.utils.js'
import { getMetaForm } from '../../js/modules/ui-helpers.js'

const DEFAULT_FIELDS = [
	{ 
		type: 'text', 
		name: 'title', 
		title: 'Title' 
	},
	{
		type: 'select',
		name: 'published',
		title: 'Published',
		options: [
			{ label: 'No', value: false },
			{ label: 'Yes', value: true }
		]
	},
	{
		type: 'date',
		name: 'publish_date',
		title: 'Publish Date',
	},
	{
		type: 'datetime',
		name: 'unpublish_date',
		title: 'Unpublish Date',
	}
]

export async function runAction(params) {

	const contentNode = await getContentNode({
		url: getPreviewUrl()
	})

	const getContentResponse = await getContent({
		uri: contentNode.result.uri
	})

	const previewMetaForm = getMetaForm()
	const fields = [
		...DEFAULT_FIELDS,
		...previewMetaForm
	]
	
	const values = {
		'title': getContentResponse?.result?.meta?.title,
		'published': getContentResponse?.result?.meta?.published,
		'publish_date': getContentResponse?.result?.meta?.publish_date,
		'unpublish_date': getContentResponse?.result?.meta?.unpublish_date,
		...buildValuesFromFields(previewMetaForm, getContentResponse?.result?.meta)
	}

	const form = createForm({
		fields: fields,
		values: values
	});

	openSidebar({
		title: 'Page meta',
		body: 'modal body',
		form: form,
		onCancel: (event) => {},
		onOk: async (event) => {
			var updateData = form.getData()
			var setMetaResponse = await setMeta({
				uri: contentNode.result.uri,
				meta: updateData
			})
			showToast({
				title: 'MetaData saved',
				message: 'MetaData successfuly saved.',
				type: 'success', // optional: info | success | warning | error
				timeout: 3000
			});
			reloadPreview()
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
		if (!field.name) continue;
		values[field.name] = getValueByPath(sourceObj, field.name);
	}
	return values;
};

