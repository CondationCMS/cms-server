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

import { updateStateButton } from './modules/manager-ui.js';
import { EventBus } from './modules/event-bus.js';

frameMessenger.on('load', (payload) => {
	EventBus.emit("preview:loaded", {});
});

document.addEventListener("DOMContentLoaded", function () {

	//PreviewHistory.init("/");
	updateStateButton();

	const iframe = document.getElementById('contentPreview');

	const preview = UIStateManager.getTabState("preview", null)
	if (preview) {
		loadPreview(preview.url)
	}

	iframe.addEventListener("load", () => {
		EventBus.emit("preview:loaded", {});
		try {
			const currentUrl = iframe.contentWindow.location.href;
			const url = new URL(currentUrl);
			const preview_url = url.pathname + url.search;

			const preview_update = {
				url: preview_url
			}

			UIStateManager.setTabState("preview", preview_update)

			updateStateButton();

		} catch (e) {
			console.log(e)
		}
	})

	frameMessenger.on('edit', (payload) => {
		if (payload.element === "content") {
			var cmd = {
				"module": window.manager.baseUrl + "/actions/page/edit-content",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor,
					"options": payload.options ? payload.options : {}
				}
			}
			if (payload.uri) {
				cmd.parameters.uri = payload.uri
			}
			executeScriptAction(cmd)
		} else if (payload.element === "meta" && payload.editor === "form") {
			var cmd = {
				"module": window.manager.baseUrl + "/actions/page/edit-metaattribute-list",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor,
					"attributes": payload.metaElements,
					"options": payload.options ? payload.options : {}
				}
			}
			if (payload.uri) {
				cmd.parameters.uri = payload.uri
			}
			executeScriptAction(cmd)
		} else if (payload.element === "image" && payload.editor === "form") {
			executeImageForm(payload);
		} else if (payload.element === "image" && payload.editor === "select") {
			executeImageSelect(payload);
		} else if (payload.element === "meta") {
			var cmd = {
				"module": window.manager.baseUrl + "/actions/page/edit-metaattribute",
				"function": "runAction",
				"parameters": {
					"editor": payload.editor,
					"attribute": payload.metaElement,
					"options": payload.options ? payload.options : {}
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
			"module": window.manager.baseUrl + "/actions/page/edit-sections",
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
			"module": window.manager.baseUrl + "/actions/page/add-section",
			"function": "runAction",
			"parameters": {
				"sectionName": payload.sectionName
			}
		}
		executeScriptAction(cmd)
	});

	frameMessenger.on('delete-section', (payload) => {
		var cmd = {
			"module": window.manager.baseUrl + "/actions/page/delete-section",
			"function": "runAction",
			"parameters": {
				"sectionUri": payload.sectionUri
			}
		}
		executeScriptAction(cmd)
	});
});
// DOMContentLoaded  end

const executeImageForm = (payload) => {
	const cmd = {
		"module": window.manager.baseUrl + "/actions/media/edit-media-form",
		"function": "runAction",
		"parameters": {
			"editor": payload.editor,
			"attribute": payload.metaElement,
			"options": payload.options ? payload.options : {}
		}
	}
	if (payload.uri) {
		cmd.parameters.uri = payload.uri
	}
	executeScriptAction(cmd);
}

const executeImageSelect = (payload) => {
	const cmd = {
		"module": window.manager.baseUrl + "/actions/media/select-image",
		"function": "runAction",
		"parameters": {
			"attribute": payload.metaElement,
			"options": payload.options ? payload.options : {}
		}
	}
	executeScriptAction(cmd);
}