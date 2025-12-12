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
import frameMessenger from '@cms/modules/frameMessenger.js';
import { loadPreview } from '@cms/modules/preview.utils.js';
import { UIStateManager } from '@cms/modules/ui-state.js';
import { updateStateButton } from '@cms/modules/manager-ui.js';
import { EventBus } from '@cms/modules/event-bus.js';
import { initMessageHandlers } from '@cms/modules/manager/manager.message.handlers.js';
import { createCSRFToken } from '@cms/modules/rpc/rpc-manager.js';
import { setCSRFToken } from '@cms/modules/utils.js';
frameMessenger.on('load', (payload) => {
    EventBus.emit("preview:loaded", {});
});
document.addEventListener("DOMContentLoaded", function () {
    //PreviewHistory.init("/");
    //updateStateButton();
    const intervalId = window.setInterval(() => {
        var token = createCSRFToken({});
        token.then((token) => {
            setCSRFToken(token.result);
        });
    }, 5 * 60 * 1000);
    const iframe = document.getElementById('contentPreview');
    const urlParams = new URLSearchParams(window.location.search);
    const pageUrl = urlParams.get('page');
    if (pageUrl) {
        loadPreview(pageUrl);
        // Clean the URL
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
    else {
        const preview = UIStateManager.getTabState("preview", null);
        if (preview) {
            if (preview.siteId === window.manager.siteId) {
                loadPreview(preview.url);
            }
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
            };
            UIStateManager.setTabState("preview", preview_update);
            updateStateButton();
        }
        catch (e) {
            console.log(e);
        }
    });
    initMessageHandlers();
});
// DOMContentLoaded  end
