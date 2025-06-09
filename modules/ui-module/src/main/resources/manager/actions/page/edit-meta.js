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

export async function runAction(params) {

	const contentNode = await getContentNode({
		url: getPreviewUrl()
	})

	const getContentResponse = await getContent({
		uri: contentNode.result.uri
	})

	const form = createForm({
		fields: [
			{ type: 'text', name: 'title', title: 'Title' },
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
			},
			{
				type: 'color',
				name: 'background_color',
				title: 'Background Color'
			},
			{
				type: "range",
				name: "range_test",
				title: "RangField"
			},
			{
				type: "radio",
				name: "choose_color",
				title: "Farbe wählen",
				choices: [
					{ label: "Rot", value: "red" },
					{ label: "Grün", value: "green" },
					{ label: "Blau", value: "blue" }
				]
			},
			{
				name: "features",
				title: "Funktionen auswählen",
				type: "checkbox",
				choices: [
					{ label: "Suche", value: "search" },
					{ label: "Filter", value: "filter" },
					{ label: "Export", value: "export" }
				]
			}


		],
		values: {
			'title': getContentResponse?.result?.meta?.title,
			'published': getContentResponse?.result?.meta?.published,
			'publish_date': getContentResponse?.result?.meta?.publish_date,
			'unpublish_date': getContentResponse?.result?.meta?.unpublish_date,
			'background_color': getContentResponse?.result?.meta?.background_color,
			'range_test': getContentResponse?.result?.meta?.range_test,
			'choose_color': getContentResponse?.result?.meta?.choose_color,
			'features': getContentResponse?.result?.meta?.features,
		}
	});



	openSidebar({
		title: 'Example Model',
		body: 'modal body',
		form: form,
		onCancel: (event) => console.log("modal canceled"),
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
