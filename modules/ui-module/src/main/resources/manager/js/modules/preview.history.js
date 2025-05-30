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
/* preview-history.js */

export const PreviewHistory = (() => {
    const iframeId = "contentPreview";
    let lastUrl = null;

    function getIframe() {
        return document.getElementById(iframeId);
    }

    function navigatePreview(url, usePush = true) {
        const iframe = getIframe();
        if (!iframe) return;

        iframe.src = url;
        lastUrl = url;

        if (usePush) {
            history.pushState({ iframeUrl: url }, "", "#" + encodeURIComponent(url));
        }
    }

    function restoreFromHash() {
        const hash = location.hash;
        if (hash && hash.length > 1) {
            const url = decodeURIComponent(hash.slice(1));
            navigatePreview(url, false);
        }
    }

    function handlePopState(event) {
        const iframe = getIframe();
        if (!iframe) return;

        if (event.state && event.state.iframeUrl) {
            iframe.src = event.state.iframeUrl;
            lastUrl = event.state.iframeUrl;
        } else {
            restoreFromHash();
        }
    }

    function setupOnloadFallback() {
        const iframe = getIframe();
        if (!iframe) return;

        iframe.onload = () => {
            try {
                const current = iframe.contentWindow.location.href;
                if (current !== lastUrl) {
                    lastUrl = current;
                    history.pushState({ iframeUrl: current }, "", "#" + encodeURIComponent(current));
                }
            } catch (e) {
                // Cross-origin, can't access
            }
        };
    }

    function init() {
        window.addEventListener("popstate", handlePopState);
        window.addEventListener("load", () => {
            //restoreFromHash();
            setupOnloadFallback();
        });
    }

    return {
        init,
        navigatePreview,
    };
})();
