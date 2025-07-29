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
export function uploadFileWithProgress({ uploadEndpoint, file, uri, onProgress, onSuccess, onError }) {
    if (!file) {
        onError?.("No file selected.");
        return;
    }
    const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    if (file.size > MAX_FILE_SIZE_BYTES) {
        onError?.(`File is too large. Maximum size is ${MAX_FILE_SIZE_BYTES} bytes.`);
        return;
    }
    const formData = new FormData();
    formData.append("file", file);
    formData.append("uri", uri);
    const xhr = new XMLHttpRequest();
    xhr.open("POST", uploadEndpoint ?? "/manager/upload", true);
    xhr.setRequestHeader("X-CSRF-Token", window.manager.csrfToken);
    xhr.upload.onprogress = (event) => {
        if (event.lengthComputable && typeof onProgress === "function") {
            const percent = Math.round((event.loaded / event.total) * 100);
            onProgress(percent);
        }
    };
    xhr.onload = () => {
        if (xhr.status === 200) {
            try {
                const json = JSON.parse(xhr.responseText);
                onSuccess?.(json); // Übergibt das JSON an den Callback
            }
            catch (e) {
                onError?.("Response is not valid JSON.");
            }
        }
        else {
            onError?.(`Upload failed: ${xhr.status} ${xhr.statusText}`);
        }
    };
    xhr.onerror = () => {
        onError?.("Upload error occurred.");
    };
    xhr.send(formData);
}
