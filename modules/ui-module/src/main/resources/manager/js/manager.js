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

import frameMessenger from './modules/frameMessenger.js';
import { loadPreview } from './modules/preview.utils.js';

import { UIStateManager } from './modules/ui-state.js';

document.addEventListener("DOMContentLoaded", function () {

	const iframe = document.getElementById('contentPreview');

	const preview = UIStateManager.getTabState("preview", null)
	if (preview) {
		console.log("restore preview url", preview.url)
		loadPreview(preview.url)
	}

	iframe.addEventListener("load", () => {
		try {
			const currentUrl = iframe.contentWindow.location.href;
			const url = new URL(currentUrl);
			const preview_url = url.pathname + url.search;

			const preview_update = {
				url: preview_url
			}

			console.log("set preview", preview_update)
			UIStateManager.setTabState("preview", preview_update)
		} catch (e) {
			console.log(e)
		}
	})

	frameMessenger.on('edit', (payload) => {
		console.log(payload)
		if (payload.element === "content") {
			var cmd = {
				"module": "/manager/actions/page/edit-content",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor
				}
			}
			if (payload.uri) {
				cmd.parameters.uri = payload.uri
			}
			executeScriptAction(cmd)
		} else if (payload.element === "meta" && payload.editor === "form") {
			var cmd = {
				"module": "/manager/actions/page/edit-metaattribute-list",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor,
					"attributes": payload.metaElements
				}
			}
			if (payload.uri) {
				cmd.parameters.uri = payload.uri
			}
			executeScriptAction(cmd)
		} else if (payload.element === "meta") {
			var cmd = {
				"module": "/manager/actions/page/edit-metaattribute",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor,
					"attribute": payload.metaElement
				}
			}
			if (payload.uri) {
				cmd.parameters.uri = payload.uri
			}
			executeScriptAction(cmd)
		}
	});
	frameMessenger.on('edit-sections', (payload) => {
		var cmd = {
			"module": "/manager/actions/page/edit-sections",
			"function": "runAction",
			"parameters": {
				"sectionName": payload.sectionName
			}
		}
		if (payload.uri) {
			cmd.parameters.uri = payload.uri
		}
		executeScriptAction(cmd)
	});

	frameMessenger.on('add-section', (payload) => {
		var cmd = {
			"module": "/manager/actions/page/add-section",
			"function": "runAction",
			"parameters": {
				"sectionTemplates": payload.sectionTemplates,
				"sectionName": payload.sectionName
			}
		}
		executeScriptAction(cmd)
	});
});
// DOMContentLoaded  end
