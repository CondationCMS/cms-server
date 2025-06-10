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
import {openModal} from '/manager/js/modules/modal.js'
import {createForm} from '/manager/js/modules/form/forms.js'
import {showToast} from '/manager/js/modules/toast.js'
import {getPreviewUrl, reloadPreview} from '/manager/js/modules/preview.utils.js'
import { getMetaValueByPath } from '/manager/js/modules/node.js'
import {getContentNode, getContent, setMeta} from '/manager/js/modules/rpc/rpc-content.js'
		// hook.js
export async function runAction(params) {


	var uri = null
	if (params.uri) {
		uri = params.uri
	} else {
		const contentNode = await getContentNode({
			url: getPreviewUrl()
		})
		uri = contentNode.result.uri
	}

	const getContentResponse = await getContent({
		uri: uri
	})

	let formDefinition = {
		fields: [],
		values: {}
	}
	
	params.attributes.forEach(attr => {
		formDefinition.values[attr.name] = getMetaValueByPath(getContentResponse?.result?.meta, attr.name)
		formDefinition.fields.push(
			{
				type: attr.editor, 
				name: attr.name, 
				title: attr.name,
				editorOptions: attr.editorOptions ? attr.editorOptions : {}
			}
		)
	})


	const form = createForm(formDefinition)

	openModal({
		title: 'Edit meta attribute',
		body: 'modal body',
		form: form,
		fullscreen: true,
		onCancel: (event) => {},
		onOk: async (event) => {
			var updateData = form.getData()
			var setMetaResponse = await setMeta({
				uri: uri,
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
