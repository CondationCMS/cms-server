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
import { createID } from "./utils.js";
import { i18n } from "../localization.js"
import { uploadFileWithProgress } from "../upload.js";
import { openFileBrowser } from "../filebrowser.js";
import { FieldOptions, FormContext, FormField } from "./forms.js";

export interface MediaFieldOptions extends FieldOptions {
}

const createMediaField = (options: MediaFieldOptions, value : string = '') => {
	const id = createID();
	const key = "field." + options.name;
	const title = i18n.t(key, options.title);
	var previewUrl = value;
	if (value && value != '') {
		previewUrl = patchPathWithContext("/assets/" + value)
	} else {
		previewUrl = "https://placehold.co/100x100"
	}

	return `
		<div class=" cms-form-field" data-cms-form-field-type="media" data-field-id="${id}">
			<div class="d-flex align-items-start">
				<div class="cms-media-preview flex-shrink-0-11 me-3">
					<img src="${previewUrl}" alt="Image preview" class="cms-media-image">
				</div>
				<div class="d-flex flex-column" style="flex-grow:1;">
					<label class="cms-drop-zone">
						<div><i class="bi bi-upload me-2"></i><span cms-i18n-key="${key}">${title}</span></div>
						<input type="file" name=${options.name} accept="image/*" class="d-none cms-media-input">
						<input type="text" name=${options.name} class="d-none cms-media-input-value" value="${value}">
					</label>
					<button type="button" class="btn btn-outline-primary mt-2 cms-media-button">
						<i class="bi bi-images me-1"></i> MediaManager
					</button>
				</div>
			</div>
		</div>
	`;
};

const getData = (context : FormContext) => {
	const data = {};
	
	context.formElement.querySelectorAll("[data-cms-form-field-type='media']").forEach(wrapper => {
		const input = wrapper.querySelector(".cms-media-input-value") as HTMLInputElement;
		if (input) {
			data[input.name] = {
				type: 'media',
				value: input.value
			};
		}
	});
	return data;
};

const init = (context : FormContext) => {
       context.formElement.querySelectorAll("[data-cms-form-field-type='media']").forEach(wrapper => {
		const dropZone = wrapper.querySelector(".cms-drop-zone");
		const input = wrapper.querySelector(".cms-media-input") as HTMLInputElement;
		const preview = wrapper.querySelector(".cms-media-image") as HTMLImageElement;
		const openMediaManager = wrapper.querySelector(".cms-media-button") as HTMLButtonElement;

		if (!input || !dropZone || !preview || !openMediaManager) return;

		// Handle file drop
		dropZone.addEventListener("dragover", (e) => {
			e.preventDefault();
			e.stopPropagation();
			dropZone.classList.add("drag-over");
		});

		dropZone.addEventListener("dragleave", (e) => {
			e.preventDefault();
			e.stopPropagation();
			dropZone.classList.remove("drag-over");
		});
		dropZone.addEventListener("drop", (e : DragEvent) => {
			e.preventDefault();
			e.stopPropagation();
			dropZone.classList.remove("drag-over");
			if (e.dataTransfer.files.length > 0) {
				input.files = e.dataTransfer.files;
				preview.src = URL.createObjectURL(e.dataTransfer.files[0]);
				var file = e.dataTransfer.files[0]
				handleUpload(wrapper, file);
			}
		});

		// Handle click to open file chooser
		//dropZone.addEventListener("click", () => input.click());

		// Handle file selection
		input.addEventListener("change", (e: Event) => {
			const file = (e.target as HTMLInputElement).files[0];
			if (file) {
				preview.src = URL.createObjectURL(file);
				handleUpload(wrapper, file);
			}
		});

			// Handle MediaManager button robust: remove old handler, use onclick
			openMediaManager.onclick = null;
			openMediaManager.onclick = () => {
				openFileBrowser({
					type: "assets",
					filter : (file) => {
						return file.media || file.directory;
					},
					onSelect: (file : any) => {
						const preview = wrapper.querySelector(".cms-media-image") as HTMLImageElement;
						const inputValue = wrapper.querySelector(".cms-media-input-value") as HTMLInputElement;
						if (file && file.uri) {
							var value = file.uri; // Use the file's URI
							if (file.uri.startsWith("/")) {
								value = file.uri.substring(1); // Remove leading slash if present
							}
							inputValue.value = value; // Set the input value to the selected file's name

							var previewUrl = value;
							if (value && value != '') {
								previewUrl = patchPathWithContext("/assets/" + value)
							} else {
								previewUrl = "https://placehold.co/100x100"
							}
							preview.src = previewUrl; // Update the preview image
						}
					}
				});
			};
	});
};

const handleUpload = (wrapper, file) => {
	const inputValue = wrapper.querySelector(".cms-media-input-value");
	uploadFileWithProgress({
		uploadEndpoint: "/manager/upload2",
		file: file,
		uri: "not relevant for media fields",
		onProgress: (percent) => {
			console.log(`Upload progress: ${percent}%`);
		},
		onSuccess: (data) => {
			if (data.filename) {
				inputValue.value = data.filename; // Set the input value to the uploaded file's name
			}
		},
		onError: (error) => {
			console.error("Upload failed:", error);
		}
	});
}

export const MediaField = {
	markup: createMediaField,
	init: init,
	data: getData
} as FormField;
