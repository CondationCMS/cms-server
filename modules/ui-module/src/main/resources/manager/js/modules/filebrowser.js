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

import { listFiles, createFolder, createFile, deleteFile, deleteFolder, renameFile } from '/manager/js/modules/rpc-files.js'
import { createPage, deletePage } from '/manager/js/modules/rpc-page.js'
import { openModal } from '/manager/js/modules/modal.js'
import Handlebars from 'https://cdn.jsdelivr.net/npm/handlebars@latest/+esm';
import { loadPreview, getPageTemplates } from '/manager/js/modules/ui-helpers.js'
import { i18n } from '/manager/js/modules/localization.js';
import { alertSelect, alertError, alertConfirm, alertPrompt } from '/manager/js/modules/alerts.js'
import { uploadFileWithProgress } from '/manager/js/modules/upload.js'
import { showToast } from '/manager/js/modules/toast.js'


const defaultOptions = {
	validate: () => true,
	uri: ""
};

const state = {
	options: null,
	currentFolder: ""
};

const allowedMimeTypes = [
		"image/png",
		"image/jpeg",
		"image/gif",
		"image/webp",
		"image/svg+xml",
		"image/tiff",
		"image/avif"
	];

const template = Handlebars.compile(`
	<div>
		<div class="dropdown">
			<button class="btn btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
				Actions
			</button>
			<ul class="dropdown-menu">
				{{#each actions}}
					<li><a class="dropdown-item" href="#" id="{{id}}">{{name}}</a></li>
				{{/each}}
			</ul>
		</div>
	<table class="table table-hover">
		<thead>
			<tr>
				<th scope="col"></th>
				<th scope="col">{{filenameHeader}}</th>
				<th scope="col">{{actionHeader}}</th>
			</tr>
		</thead>
		<tbody id="cms-filebrowser-files">
		{{#each files}}
			<tr 
				data-cms-file-uri="{{uri}}"
				data-cms-file-name="{{name}}"
				{{#if directory}} data-cms-file-directory="true"{{/if}}>
				<th scope="row">
					{{#if directory}}
						<i class="bi bi-folder"></i>
					{{else}}
						<i class="bi bi-file"></i>
					{{/if}}
				</th>
				<td>{{name}}</td>
				<td>
					{{#if directory}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFolder"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Delete folder."
						>
							<i class="bi bi-folder-x"></i>
						</button>
					{{else if content}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="open"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Open page."
						>
							<i class="bi bi-file-arrow-up"></i>
							</button>
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deletePage"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Delete page."
						>
							<i class="bi bi-file-earmark-x"></i>
						</button>
					{{else}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFile"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Delete file."
						>
							<i class="bi bi-file-earmark-x"></i>
						</button>
					{{/if}}
					<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="renameFile"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Rename file."
						>
							<i class="bi bi-pencil-square"></i>
						</button>
				</td>
			<tr>
		{{/each}}
		</tbody>
	</table>
	{{#if asset}} 
		<input id="cms-fileupload" type="file" name="cms-fileupload" accept="image/png, image/jpeg, image/web, image/gif, image/svg+xml, image/tiff, image/avif" />
		<button id="cms-filebrowser-upload-button"> Upload </button>
		<span id="cms-filebrowser-upload-progress"></span>
	{{/if}}
	</div>
`);

const openFileBrowser = async (optionsParam) => {
	state.options = {
		...defaultOptions,
		...optionsParam
	};

	state.modal = openModal({
		title: i18n.t("ui.filebrowser.title", "Filesystem"),
		body: '<div id="cms-file-browser"></div>',
		fullscreen: true,
		onOk: async (event) => {
			console.log("modal ok");
		},
		onShow: async () => {
			initFileBrowser();
		}
	});

};

const createPageHandler = async (filename) => {
	const templateMap = getPageTemplates().reduce((acc, { name, template }) => {
		acc[template] = name;
		return acc;
	}, {});


	var template = await alertSelect({
		title: "Select page template",
		placeholder: "Select a page template",
		values: templateMap
	})

	if (!template) {
		alertError({
			message: "No template selected"
		})
		return
	}

	var parent = state.currentFolder
	if (state.currentFolder.startsWith("/")) {
		parent = state.currentFolder.substring(1);
	}

	let response = await createPage({
		uri: parent,
		name: filename,
		meta: {
			title: "New of: " + template,
			template: template,
			published: false
		}
	});
	if (response.error) {
		showToast({
			title: 'Error creating folder',
			message: response.error.message,
			type: 'error', // optional: info | success | warning | error
			timeout: 3000
		});
	} else {
		await initFileBrowser(state.currentFolder);
	}
}

const createFolderHandler = async (folderName) => {
	let response = await createFolder({
		uri: state.currentFolder,
		name: folderName,
		type: state.options.type
	});
	if (response.error) {
		showToast({
			title: 'Error creating folder',
			message: response.error.message,
			type: 'error', // optional: info | success | warning | error
			timeout: 3000
		});
	} else {
		await initFileBrowser(state.currentFolder);
	}
}

const createFileHandler = async (filename) => {
	let response = await createFile({
		uri: state.currentFolder,
		name: filename,
		type: state.options.type
	});
	if (response.error) {
		showToast({
			title: 'Error creating file',
			message: response.error.message,
			type: 'error', // optional: info | success | warning | error
			timeout: 3000
		});
	} else {
		await initFileBrowser(state.currentFolder);
	}
}

const handleCreateElement = async (type, folderName) => {
	console.log("Creating folder:", folderName);

	if (type === "page") {
		createPageHandler(folderName)
	} else if (type === "folder") {
		createFolderHandler(folderName)
	} else if (type === "file") {
		createFileHandler(folderName)
	}
};

const insertElementInputRow = async (options) => {
	const tableBody = document.getElementById("cms-filebrowser-files");
	if (!tableBody) return;

	// Prüfe, ob bereits eine Eingabezeile existiert
	if (document.getElementById("cms-new-element-row")) return;

	const row = document.createElement("tr");
	row.id = "cms-new-element-row";
	row.innerHTML = `
		<th scope="row"><i class="bi bi-${options.type}-plus"></i></th>
		<td><input id="cms-new-element-input" class="form-control" type="text" placeholder="${i18n.t("ui.filebrowser.enter.elementname", "Enter Element name")}" /></td>
		<td></td>
	`;
	tableBody.prepend(row);

	const input = document.getElementById("cms-new-element-input");
	input.focus();

	input.addEventListener("keydown", (event) => {
		event.stopPropagation()
		if (event.key === "Enter") {
			event.preventDefault();
			const folderName = input.value.trim();
			if (folderName.length > 0) {
				handleCreateElement(options.type, folderName);
			} else {
				removeElementInputRow();
			}
		} else if (event.key === "Escape") {
			event.preventDefault();
			removeElementInputRow();
		}
	});
};

const removeElementInputRow = () => {
	const existingRow = document.getElementById("cms-new-element-row");
	if (existingRow) {
		existingRow.remove();
	}
};

const initFileBrowser = async (uri) => {
	state.currentFolder = uri ? uri : "";

	const contentFiles = await listFiles({
		type: state.options.type,
		uri: state.currentFolder
	});

	const fileBrowserElement = document.getElementById("cms-file-browser");
	if (fileBrowserElement) {
		fileBrowserElement.innerHTML = template({
			files: contentFiles.result.files,
			filenameHeader: i18n.t("ui.filebrowser.filename", "Filename"),
			actionHeader: i18n.t("ui.filebrowser.action", "Action"),
			actions: getActions(),
			asset : state.options.type === "assets"
		});
		makeDirectoriesClickable();
		fileActions();
		initBootstrapTooltips();
	}
};
const initBootstrapTooltips = () => {
	const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
	tooltipTriggerList.forEach((tooltipTriggerEl) => {
		new bootstrap.Tooltip(tooltipTriggerEl);
	});
};

const getActions = () => {
	const actions = []

	if (state.options.type === "content") {
		actions.push({
			id: "cms-filebrowser-action-createPage",
			name: i18n.t("ui.filebrowser.create.page", "Create page")
		})
	}
	actions.push({
		id: "cms-filebrowser-action-createFile",
		name: i18n.t("ui.filebrowser.create.file", "Create file")
	})
	actions.push({
		id: "cms-filebrowser-action-createFolder",
		name: i18n.t("ui.filebrowser.create.folder", "Create folder")
	})

	return actions
}

const makeDirectoriesClickable = () => {
	const elements = document.querySelectorAll("[data-cms-file-directory]");
	elements.forEach((element) => {
		element.addEventListener("dblclick", (event) => {
			event.stopPropagation();
			const directory = element.getAttribute("data-cms-file-uri");
			if (directory) {
				initFileBrowser(directory);
			}
		});
	});
};

const deleteElementAction = async (options) => {
	
	var confimred = await alertConfirm({
		title: i18n.t("ui.filebrowser.delete.confirm.title", "Are you sure?"),
		message: i18n.t("ui.filebrowser.delete.confirm.message", "You won't be able to revert this!"),
		confirmText: i18n.t("ui.filebrowser.delete.confirm.yes", "Yes, delete it!"),
		cancelText: i18n.t("ui.filebrowser.delete.confirm.no", "No, cancel!")
	});
	if (!confimred) {
		return;
	}

	var parent = state.currentFolder
	if (state.currentFolder.startsWith("/")) {
		parent = state.currentFolder.substring(1);
	}
	var response = await options.deleteFN({
		uri: parent,
		name : options.uri,
		type: state.options.type
	})
	if (response.error) {
		showToast({
			title: 'Error deleting',
			message: response.error.message,
			type: 'error', // optional: info | success | warning | error
			timeout: 3000
		});
	} else {
		showToast({
			title: 'Element deleted',
			message: "Element deleted",
			type: 'info', // optional: info | success | warning | error
			timeout: 3000
		});
		await initFileBrowser(state.currentFolder);
	}
}

const renameFileAction = async (filename) => {
	const newName = await alertPrompt({
		title: i18n.t("ui.filebrowser.rename.title", "Rename file"),
		label: i18n.t("ui.filebrowser.rename.label", "New name"),
		placeholder: filename
	});
	if (newName) {
		var response = await renameFile({
			uri: getTargetFolder(),
			name: filename,
			newName: newName,
			type: state.options.type
		});
		if (response.error) {
			showToast({
				title: 'Error renaming file',
				message: response.error.message,
				type: 'error', // optional: info | success | warning | error
				timeout: 3000
			});
		} else {
			showToast({
				title: 'File renamed',
				message: "File renamed successfully",
				type: 'info', // optional: info | success | warning | error
				timeout: 3000
			});
			await initFileBrowser(state.currentFolder);
		}
	}
}

const fileActions = () => {
	const elements = document.querySelectorAll("[data-cms-file-action]");
	elements.forEach((element) => {
		element.addEventListener("click", async (event) => {
			event.stopPropagation();
			const uri = element.getAttribute("data-cms-file-uri");
			const filename = element.closest("[data-cms-file-name]").dataset.cmsFileName
			const action = element.getAttribute("data-cms-file-action");

			if (action === "open") {
				await loadPreview(uri);
				state.modal.hide();
			} else if (action === "deletePage") {
				await deleteElementAction({
					uri: filename,
					deleteFN: deletePage
				})
			} else if (action === "deleteFile") {
				await deleteElementAction({
					uri: filename,
					deleteFN: deleteFile
				})
			} else if (action === "deleteFolder") {
				await deleteElementAction({
					uri: filename,
					deleteFN: deleteFolder
				})
			} else if (action === "renameFile") {
				await renameFileAction(filename);
			}
		});
	});

	document.getElementById("cms-filebrowser-action-createFolder").addEventListener("click", async (event) => {
		await insertElementInputRow({ type: "folder" });
	})
	document.getElementById("cms-filebrowser-action-createFile").addEventListener("click", async (event) => {
		await insertElementInputRow({ type: "file" });
	})
	if (document.getElementById("cms-filebrowser-action-createPage")) {
		document.getElementById("cms-filebrowser-action-createPage").addEventListener("click", async (event) => {
			await insertElementInputRow({ type: "page" });
		})
	}

	if (document.getElementById("cms-filebrowser-upload-button")) {
		document.getElementById("cms-filebrowser-upload-button").addEventListener("click", async (event) => {
			await handleFileUpload();
		});
	}

};

const handleFileUpload = async () => {
	const fileInput = document.getElementById("cms-fileupload");
	if (fileInput.files.length === 0) {
		showToast({
			title: 'No file selected',
			message: 'Please select a file to upload.',
			type: 'warning',
			timeout: 3000
		});
		return;
	}


	const file = fileInput.files[0];

	if (!allowedMimeTypes.includes(file.type)) {
		showToast({
			title: 'Invalid file type',
			message: `Only images (PNG, JPG, GIF, BMP, WEBP, TIFF, SVG, AVIF) are allowed. Selected: ${file.type}`,
			type: 'error',
			timeout: 4000
		});
		return;
	}

	let formData = new FormData();
	formData.append("file", file);
	formData.append("uri", getTargetFolder());
	uploadFileWithProgress({
		file,
		uri: getTargetFolder(),
		onProgress: (percent) => {
			updateProgressBar(percent);
		},
		onSuccess: () => {
			showToast({
				title: 'Upload complete',
				message: 'File uploaded successfully.',
				type: 'success'
			});
			updateProgressBar(100);
			initFileBrowser(state.currentFolder); 
		},
		onError: (message) => {
			showToast({
				title: 'Upload failed',
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

export { openFileBrowser };
