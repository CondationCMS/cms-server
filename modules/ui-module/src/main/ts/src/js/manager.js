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

import frameMessenger from '@cms/modules/frameMessenger.js';
import { activatePreviewOverlay, getPreviewFrame, loadPreview } from '@cms/modules/preview.utils.js';

import { UIStateManager } from '@cms/modules/ui-state.js';

import { updateStateButton } from '@cms/modules/manager-ui.js';
import { EventBus } from '@cms/modules/event-bus.js';
import { initMessageHandlers } from '@cms/modules/manager/manager.message.handlers.js';
import { createCSRFToken } from '@cms/modules/rpc/rpc-manager.js';
import { setCSRFToken } from '@cms/modules/utils.js';

frameMessenger.on('load', (payload) => {
	EventBus.emit("preview:loaded", {});
});

function heartbeat() {
    fetch(window.manager.refreshUrl, {
        method: "POST",
        credentials: "include"
    })
    .then(res => res.json())
    .then(data => {
        window.manager.previewToken = data.previewToken;
    });
}

document.addEventListener("DOMContentLoaded", function () {

	setInterval(() => {
		heartbeat();
	}, 5 *60 * 1000);

	//PreviewHistory.init("/");
	//updateStateButton();

	activatePreviewOverlay();

	const intervalId = window.setInterval(() =>  {
		var token = createCSRFToken({});
		token.then((token) => {
			setCSRFToken(token.result);
		})
	}, 5 * 60 * 1000);

	const iframe = getPreviewFrame();
	iframe.addEventListener("load", previewLoadedHandler)

	const urlParams = new URLSearchParams(window.location.search);
	const pageUrl = urlParams.get('page');

	/*
		page param is use for deeplinks when changing translation
	*/
	if (pageUrl) {
		loadPreview(pageUrl);
		// Clean the URL
		const newUrl = window.location.pathname;
		window.history.replaceState({}, document.title, newUrl);
	} else {
		const preview = UIStateManager.getTabState("preview", null);
		if (preview && preview.siteId === window.manager.siteId) {
			loadPreview(preview.url);
		} else {
			loadPreview(window.manager.previewUrl);
		}
	}

	initMessageHandlers();

});

const previewLoadedHandler = () => {
	try {
		const iframe = getPreviewFrame();

		const currentUrl = iframe.contentWindow.location.href;
		const url = new URL(currentUrl);
		const preview_url = url.pathname + url.search;

		const preview_update = {
			url: preview_url,
			siteId: window.manager.siteId
		}

		UIStateManager.setTabState("preview", preview_update)

		updateStateButton();
		EventBus.emit("preview:loaded", {});
	} catch (e) {
		console.log(e)
	}
}
// DOMContentLoaded  end
