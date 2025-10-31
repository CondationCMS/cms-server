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

import { EventBus } from "./event-bus.js";

//PreviewHistory.init();
// close overlay on preview loaded
EventBus.on("preview:loaded", (data) => {
	deActivatePreviewOverlay();
});

export const activatePreviewOverlay = () => {
	const overlay = document.getElementById("previewOverlay");
	if (overlay) {
		overlay.style.display = "flex";
	}
}
export const deActivatePreviewOverlay = () => {
	const overlay = document.getElementById("previewOverlay");
	if (overlay) {
		overlay.style.display = "none";
	}
}

const getPreviewFrame = () => {
	return document.getElementById("contentPreview");
};

const getPreviewUrl = () => {
	try {
		return getPreviewFrame().contentWindow.location.href;
	} catch (e) {
		console.warn("Konnte iframe-URL nicht auslesen", e);
		return "";
	}
}

const reloadPreview = () => {
	activatePreviewOverlay();
	getPreviewFrame().contentDocument.location.reload(true);
}

const loadPreview = (url) => {
	activatePreviewOverlay();
	
	try {
		// Fallback-Host für relative URLs, damit URL-Parsing funktioniert
		const dummyBase = window.location.origin;
		const parsedUrl = new URL(url, dummyBase);

		// Wenn "preview" bereits gesetzt ist, nicht erneut hinzufügen
		if (!parsedUrl.searchParams.has("preview")) {
			parsedUrl.searchParams.append("preview", "manager");
		}
		parsedUrl.searchParams.delete("nocache");
		parsedUrl.searchParams.append("nocache", Date.now());

		// Setze zusammengesetzten Pfad + Query zurück in das iframe
		const result = parsedUrl.pathname + parsedUrl.search;
		document.getElementById("contentPreview").src = result;
		//PreviewHistory.navigatePreview(result);

	} catch (e) {
		console.error("Ungültige URL:", url, e);
	}
}


export { getPreviewUrl, reloadPreview, loadPreview, getPreviewFrame };
