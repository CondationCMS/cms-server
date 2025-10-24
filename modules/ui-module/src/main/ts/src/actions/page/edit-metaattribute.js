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
import {createForm} from '../../js/modules/form/forms.js'
import {showToast} from '../../js/modules/toast.js'
import {getPreviewUrl, reloadPreview} from '../../js/modules/preview.utils.js'
import { getValueByPath } from '../../js/modules/node.js'
import { getContentNode, setMeta, getContent} from '../../js/modules/rpc/rpc-content.js'
import { i18n } from '../../js/modules/localization.js'
import { openSidebar } from '../../js/modules/sidebar.js'
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
		fields: [
			{
				type: params.editor, 
				options: params.options ? params.options : {},
				name: params.attribute, 
				title: "Edit attribute: " + params.attribute
			}
		],
		values: {}
	}
	formDefinition.values[params.attribute] = getValueByPath(getContentResponse?.result?.meta, params.attribute)

	const form = createForm(formDefinition)

	openSidebar({
		title: 'Edit meta attribute',
		body: 'modal body',
		form: form,
		onCancel: (event) => {},
		onOk: async (event) => {
			var updateData = form.getData()
			var setMetaResponse = await setMeta({
				uri: uri,
				meta: updateData
			})
			showToast({
				title: i18n.t('manager.actions.page.edit-metaattribute.toast.title', "MetaData updated"),
				message: i18n.t('manager.actions.page.edit-metaattribute.toast.message', "The metadata has been updated successfully."),
				type: 'success', // optional: info | success | warning | error
				timeout: 3000
			});
			reloadPreview()
		}
	});
}
