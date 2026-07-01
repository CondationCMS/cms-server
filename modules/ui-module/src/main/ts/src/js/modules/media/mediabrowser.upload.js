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

import { showToast } from '@cms/modules/toast.js'
import { i18n } from '@cms/modules/localization.js';
import { state } from '@cms/modules/media/mediabrowser.js';
import { uploadFileWithProgress} from '@cms/modules/upload.js'
import { EventBus } from '@cms/modules/event-bus.js';

const allowedMimeTypes = [
	"image/png",
	"image/jpeg",
	"image/gif",
	"image/webp",
	"image/svg+xml",
	"image/tiff",
	"image/avif"
];

export const initDragAndDropUpload = () => {
	const dropZone = document.getElementById('drop-zone');
	const fileInput = document.getElementById('cms-fileupload');

	if ( !dropZone || !fileInput ) {
		return;
	}

	// Prevent default drag behaviors; combine preventDefault+stopPropagation with per-event logic
	dropZone.addEventListener('dragenter', e => { e.preventDefault(); e.stopPropagation(); });
	dropZone.addEventListener('dragover', e => {
		e.preventDefault();
		e.stopPropagation();
		dropZone.classList.add('dragover');
	});
	dropZone.addEventListener('dragleave', e => {
		e.preventDefault();
		e.stopPropagation();
		dropZone.classList.remove('dragover');
	});
	dropZone.addEventListener('drop', e => {
		e.preventDefault();
		e.stopPropagation();
		dropZone.classList.remove('dragover');
		const files = e.dataTransfer.files;
		fileInput.files = files;
		uploadFile(files[0]);
	});
}

export const handleFileUpload = async () => {
	const fileInput = document.getElementById("cms-fileupload");
	if (fileInput.files.length === 0) {
		showToast({
			title: i18n.t("filebrowser.file.upload.no.selection.title", 'No file selected'),
			message: i18n.t("filebrowser.file.upload.no.selection.message", 'Please select a file to upload.'),
			type: 'warning',
			timeout: 3000
		});
		return;
	}


	const file = fileInput.files[0];
	uploadFile(file)
}	

const uploadFile = async (file) => {
	if (!allowedMimeTypes.includes(file.type)) {
		showToast({
			title: i18n.t("filebrowser.file.upload.wrong.type.title", 'Invalid file type'),
			message: i18n.t("filebrowser.file.upload.wrong.type.message", `Only images (PNG, JPG, GIF, BMP, WEBP, TIFF, SVG, AVIF) are allowed. Selected: ${file.type}`),
			type: 'error',
			timeout: 4000
		});
		return;
	}

	const targetFolder = getTargetFolder();
	uploadFileWithProgress({
		file,
		uri: targetFolder,
		onProgress: (percent) => {
			updateProgressBar(percent);
		},
		onSuccess: () => {
			showToast({
				title: i18n.t("filebrowser.file.upload.success.title", 'Upload complete'),
				message: i18n.t("filebrowser.file.upload.success.message", 'File uploaded successfully.'),
				type: 'success'
			});
			updateProgressBar(100);
			EventBus.emit("upload:success", {});
		},
		onError: (message) => {
			showToast({
				title: i18n.t("filebrowser.file.upload.error.title", "Upload failed"),
				message,
				type: 'error'
			});
			updateProgressBar(0);
		}
	});
}

const updateProgressBar = (percent) => {
	const progressBar = document.getElementById("cms-filebrowser-upload-progress");
	if (!progressBar) return;

	if (percent === 0) {
		progressBar.textContent = "";
	} else {
		progressBar.textContent = `Upload progress: ${percent}%`;
	}
}

const getTargetFolder = () => {
	if (state.currentFolder.startsWith("/")) {
		return state.currentFolder.substring(1);
	}
	return state.currentFolder;
};
