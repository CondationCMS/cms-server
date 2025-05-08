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
import {openModal} from '/manager/js/modal.js'
import {createForm} from '/manager/js/forms.js'
import {executeCommand} from '/manager/js/system-commands.js'
import {getPreviewUrl, reloadPreview} from '/manager/js/ui-helpers.js'
		// hook.js
export async function runAction(params) {

	const contentNode = await executeCommand({
		command: "getContentNode",
		parameters: {
			url: getPreviewUrl()
		}
	})

	const getContent = await executeCommand({
		command: "getContent",
		parameters: {
			uri: contentNode.result.uri
		}
	})

	const form = createForm({
		fields: [
			{type: 'editor', name: 'content', title: 'Inhalt'}
		],
		values: {
			"content" : getContent?.result?.content
		}
	});

	openModal({
		title: 'Edit Content',
		body: 'modal body',
		form: form,
		fullscreen: true,
		onCancel: (event) => console.log("modal canceled"),
		onOk: async (event) => {
			var updateData = form.getData()
			var setContent = await executeCommand({
				command: "setContent",
				parameters: {
					uri: contentNode.result.uri,
					content: updateData.content
				}
			})
			reloadPreview()
		}
	});
}
