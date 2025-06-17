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

const createMediaField = (options, value = '') => {
	const id = createID();
	const key = "field." + options.name;
	const title = i18n.t(key, options.title);
	const imageSrc = value !== "" ? patchPathWithContext(value) : "https://placehold.co/100x100";

	return `
		<div class="d-flex align-items-start cms-form-field" data-cms-form-field-type="media" data-field-id="${id}">
			<div class="cms-media-preview flex-shrink-0-11 me-3">
				<img src="${imageSrc}" alt="Image preview" class="cms-media-image">
			</div>
			<div class="d-flex flex-column">
				<label class="cms-drop-zone">
					<div><i class="bi bi-upload me-2"></i><span cms-i18n-key="${key}">${title}</span></div>
					<input type="file" name=${options.name} accept="image/*" class="cms-media-input d-none">
				</label>
				<button type="button" class="btn btn-outline-primary mt-2 cms-media-button">
					<i class="bi bi-images me-1"></i> MediaManager
				</button>
			</div>
		</div>
	`;
};

const getData = () => {
	const data = {};
	document.querySelectorAll("[data-cms-form-field-type='media']").forEach(wrapper => {
		const input = wrapper.querySelector(".cms-media-input");
		if (input) {
			data[input.name] = {
				type: 'media',
				value: input.value
			};
		}
	});
	return data;
};

const init = () => {
	document.querySelectorAll("[data-cms-form-field-type='media']").forEach(wrapper => {
		const dropZone = wrapper.querySelector(".cms-drop-zone");
		const input = wrapper.querySelector(".cms-media-input");
		const preview = wrapper.querySelector(".cms-media-image");
		const button = wrapper.querySelector(".cms-media-button");

		if (!input || !dropZone || !preview || !button) return;

		// Assign a name to the input if not already
		if (!input.name) {
			const id = wrapper.getAttribute("data-field-id");
			input.name = id;
		}

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
		dropZone.addEventListener("drop", (e) => {
			e.preventDefault();
			e.stopPropagation();
			dropZone.classList.remove("drag-over");
			if (e.dataTransfer.files.length > 0) {
				input.files = e.dataTransfer.files;
				preview.src = URL.createObjectURL(e.dataTransfer.files[0]);

				var file = e.dataTransfer.files[0]
				uploadFileWithProgress({
					uploadEndpoint: "/manager/upload2",
					file: file,
					uri: "not relevant for media fields",
					onProgress: (percent) => {
						console.log(`Upload progress: ${percent}%`);
					},
					onSuccess: (data) => {
						if (data.filename) {
							input.value = data.filename; // Set the input value to the uploaded file's name
						}
					},
					onError: (error) => {
						console.error("Upload failed:", error);
					}
				});
			}
		});

		// Handle click to open file chooser
		dropZone.addEventListener("click", () => input.click());

		// Handle file selection
		input.addEventListener("change", (e) => {
			const file = e.target.files[0];
			if (file) {
				preview.src = URL.createObjectURL(file);
			}
		});

		// Handle MediaManager button
		button.addEventListener("click", () => {
			// Hier könntest du ein Modal öffnen oder deine Mediensuche starten
			alert("MediaManager öffnen – TODO: Implementieren");
		});
	});
};

export const MediaField = {
	markup: createMediaField,
	init: init,
	data: getData
};
