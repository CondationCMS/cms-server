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
export const PreviewHistory = (() => {
    const iframeId = "contentPreview";
    const storageKey = "cms-previewHistoryStack";
    let lastUrl = null;

    function getIframe() {
        return document.getElementById(iframeId);
    }

    function getStack() {
        try {
            return JSON.parse(sessionStorage.getItem(storageKey)) || [];
        } catch (e) {
            return [];
        }
    }

    function setStack(stack) {
        sessionStorage.setItem(storageKey, JSON.stringify(stack));
    }

    function getIndexFromHash() {
        const hash = location.hash;
        const index = parseInt(hash.replace("#", ""), 10);
        return isNaN(index) ? null : index;
    }

    function setIndexInHash(index) {
        history.pushState({ iframeIndex: index }, "", `#${index}`);
    }

    function navigatePreview(url, usePush = true) {
        const iframe = getIframe();
        if (!iframe) return;

        const stack = getStack();
        stack.push(url);
        setStack(stack);

        const index = stack.length - 1;
        iframe.src = url;
        lastUrl = url;

        if (usePush) {
            setIndexInHash(index);
        }
    }

    function restoreFromStack() {
        const iframe = getIframe();
        if (!iframe) return;

        const stack = getStack();
        const index = getIndexFromHash();

        if (index !== null && stack[index]) {
            iframe.src = stack[index];
            lastUrl = stack[index];
        } else if (stack.length > 0) {
            const lastIndex = stack.length - 1;
            iframe.src = stack[lastIndex];
            lastUrl = stack[lastIndex];
            setIndexInHash(lastIndex);
        }
    }

    function handlePopState(event) {
        restoreFromStack();
    }

    function setupOnloadFallback() {
        const iframe = getIframe();
        if (!iframe) return;

        iframe.onload = () => {
            try {
                const current = iframe.contentWindow.location.href;
                if (current !== lastUrl) {
                    const stack = getStack();
                    stack.push(current);
                    setStack(stack);
                    const index = stack.length - 1;
                    setIndexInHash(index);
                    lastUrl = current;
                }
            } catch (e) {
                // Cross-origin – nichts tun
            }
        };
    }

    function init(defaultUrl = null) {
        window.addEventListener("popstate", handlePopState);
        window.addEventListener("load", () => {
            const stack = getStack();
            if (stack.length === 0 && defaultUrl) {
                navigatePreview(defaultUrl);
            } else {
                restoreFromStack();
            }
            setupOnloadFallback();
        });
    }

    return {
        init,
        navigatePreview,
    };
})();

