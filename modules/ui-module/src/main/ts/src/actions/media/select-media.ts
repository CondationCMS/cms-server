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

import { openMediaBrowser } from "@cms/modules/media/mediabrowser.js";
import { i18n } from "@cms/modules/localization.js";
import { getPreviewUrl, reloadPreview } from "@cms/modules/preview.utils.js";
import { getContentNode, setMeta } from "@cms/modules/rpc/rpc-content.js";
import { showToast } from "@cms/modules/toast.js";

export async function runAction(params : any) {


	var uri : any = null
	if (params.options.uri) {
		uri = params.options.uri
	} else {
		const contentNode = await getContentNode({
			url: getPreviewUrl()
		})
		uri = contentNode.result.uri
	}

	openMediaBrowser({
		type: "assets",
		filter : (file: any) => {
			return file.media || file.directory;
		},
		onSelect: async (file: any) => {

			if (file && file.uri) {

				var selectedFile = file.uri; // Use the file's URI
				if (file.uri.startsWith("/")) {
					selectedFile = file.uri.substring(1); // Remove leading slash if present
				}

				var updateData : any = {}
				updateData[params.options.metaElement] = {
					type: 'media',
					value: selectedFile
				}
				try {
					await setMeta({
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
				} catch (e) {
					showToast({
						title: i18n.t('manager.actions.media.select-media.toast.error.title', "Media not updated"),
						message: (e as Error).message,
						type: 'error', // optional: info | success | warning | error
						timeout: 3000
					});
				}
			}
		}
	})
}
