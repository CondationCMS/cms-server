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
import { openModal } from '@cms/modules/modal.js'
import { createForm } from '@cms/modules/form/forms.js'
import { showToast } from '@cms/modules/toast.js'
import { getContentNode, setMeta, getContent } from '@cms/modules/rpc/rpc-content.js'
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js'
import { i18n } from '@cms/modules/localization.js'
import { getPageTemplates } from '@cms/modules/rpc/rpc-manager.js'
import { buildValuesFromFields } from '@cms/modules/node.js'

const DEFAULT_FIELDS = [
	{
		type: 'datetime',
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

	//const previewMetaForm = getMetaForm()
	const fields = [
		...DEFAULT_FIELDS,
	]
	

	const values = {
		'publish_date': getContentResponse?.result?.meta?.publish_date,
		'unpublish_date': getContentResponse?.result?.meta?.unpublish_date,
	}

	const form = createForm({
		fields: fields,
		values: values
	});

	openModal({
		title: 'Edit schedule',
		body: 'modal body',
		form: form,
		resizable: false,
		onCancel: (event) => {},
		onOk: async (event) => {
			var updateData = form.getData()
			var setMetaResponse = await setMeta({
				uri: contentNode.result.uri,
				meta: updateData
			})
			showToast({
				title: i18n.t('manager.actions.page.edit-schedule.toast.title', "Schedule date updated"),
				message: i18n.t('manager.actions.page.edit-schedule.toast.message', "The schedule date has been updated successfully."),
				type: 'success', // optional: info | success | warning | error
				timeout: 3000
			});
			reloadPreview()
		}
	});
}
