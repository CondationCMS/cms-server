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
import { initMessageHandlers } from './modules/manager/manager.message.handlers.js';

frameMessenger.on('load', (payload) => {
	EventBus.emit("preview:loaded", {});
});


document.addEventListener("DOMContentLoaded", function () {

	//PreviewHistory.init("/");
	//updateStateButton();

	const iframe = document.getElementById('contentPreview');

	const preview = UIStateManager.getTabState("preview", null)
	if (preview) {
		if (preview.siteId === window.manager.siteId) {
			loadPreview(preview.url);
		}
	}

	iframe.addEventListener("load", () => {
		EventBus.emit("preview:loaded", {});
		try {
			const currentUrl = iframe.contentWindow.location.href;
			const url = new URL(currentUrl);
			const preview_url = url.pathname + url.search;

			const preview_update = {
				url: preview_url,
				siteId: window.manager.siteId
			}

			UIStateManager.setTabState("preview", preview_update)

			updateStateButton();
		} catch (e) {
			console.log(e)
		}
	})

	initMessageHandlers();

});
// DOMContentLoaded  end