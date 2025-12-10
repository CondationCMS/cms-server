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

import { openFileBrowser } from "@cms/modules/filebrowser.js";
import { i18n } from "@cms/modules/localization.js";
import { getPreviewUrl, reloadPreview } from "@cms/modules/preview.utils.js";
import { getContentNode, setMeta } from "@cms/modules/rpc/rpc-content.js";
import { showToast } from "@cms/modules/toast.js";

export async function runAction(params) {


	var uri = null
	if (params.options.uri) {
		uri = params.options.uri
	} else {
		const contentNode = await getContentNode({
			url: getPreviewUrl()
		})
		uri = contentNode.result.uri
	}

	openFileBrowser({
		type: "assets",
		filter : (file) => {
			return file.media || file.directory;
		},
		onSelect: async (file: any) => {

			if (file && file.uri) {

				var selectedFile = file.uri; // Use the file's URI
				if (file.uri.startsWith("/")) {
					selectedFile = file.uri.substring(1); // Remove leading slash if present
				}

				var updateData = {}
				updateData[params.options.metaElement] = {
					type: 'media',
					value: selectedFile
				}
				var setMetaResponse = await setMeta({
					uri: uri,
					meta: updateData
				})
				showToast({
					title: i18n.t('manager.actions.media.select-media.toast.title', "Media updated"),
					message: i18n.t('manager.actions.media.select-media.toast.message', "New media has been updated successfully."),
					type: 'success', // optional: info | success | warning | error
					timeout: 3000
				});
				reloadPreview()
			}
		}
	})
}