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
// uploadFileWithProgress.js

/**
 * Lädt eine Datei mit Fortschrittsanzeige via XMLHttpRequest hoch.
 * @param {File} file - Die hochzuladende Datei.
 * @param {string} uri - Der Zielordner relativ zum Server-Ausgabeordner.
 * @param {function(percent: number): void} onProgress - Callback für Fortschrittsanzeige (0–100).
 * @param {function(): void} onSuccess - Callback bei Erfolg.
 * @param {function(error: string): void} onError - Callback bei Fehler.
 */
export function uploadFileWithProgress({ file, uri, onProgress, onSuccess, onError }) {
	if (!file) {
		onError("No file selected.");
		return;
	}

	const formData = new FormData();
	formData.append("file", file);
	formData.append("uri", uri);

	const xhr = new XMLHttpRequest();
	xhr.open("POST", "/manager/upload", true);
	xhr.setRequestHeader("X-CSRF-Token", window.csrfToken);

	xhr.upload.onprogress = (event) => {
		if (event.lengthComputable && typeof onProgress === "function") {
			const percent = Math.round((event.loaded / event.total) * 100);
			onProgress(percent);
		}
	};

	xhr.onload = () => {
		if (xhr.status === 200) {
			if (typeof onSuccess === "function") onSuccess();
		} else {
			if (typeof onError === "function") onError(`Upload failed: ${xhr.statusText}`);
		}
	};

	xhr.onerror = () => {
		if (typeof onError === "function") onError("Upload error occurred.");
	};

	xhr.send(formData);
}
