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

import { listFiles, deleteFile, deleteFolder, } from './rpc/rpc-files.js'
import { deletePage } from './rpc/rpc-page.js'
import { openModal } from './modal.js'
import Handlebars from '../libs/handlebars.min.js';
import { loadPreview } from './preview.utils.js'
import { i18n } from './localization.js';

import { renameFileAction, deleteElementAction, createFolderAction, createFileAction, createPageAction } from './filebrowser.actions.js'
import { initDragAndDropUpload, handleFileUpload } from './filebrowser.upload.js';
import { EventBus } from './event-bus.js';

const defaultOptions = {
	validate: () => true,
	uri: "",
	onSelect: null
};

const state = {
	options: null,
	currentFolder: ""
};

Handlebars.registerHelper("patchPathWithContext", patchPathWithContext);
Handlebars.registerHelper('concat', function (...args) {
  args.pop();
  return args.join('');
});

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
					{{else if media}}
						<div class="position-relative d-inline-block cms-image-hover-wrapper">
							<img src="{{patchPathWithContext (concat "/assets" uri)}}" alt="{{name}}" class="img-thumbnail cms-small-image" />
							<div class="cms-overlay-image">
								<img src="{{patchPathWithContext (concat "/assets" uri)}}" alt="Zoom" class="cms-enlarged-image" />
							</div>
						</div>
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
		<div id="drop-zone">Drop files here</div>
	{{/if}}
	</div>
`);

EventBus.on("upload:success", (folder) => {
	initFileBrowser(state.currentFolder);
});

const openFileBrowser = async (optionsParam) => {
	state.options = {
		...defaultOptions,
		...optionsParam
	};

	state.modal = openModal({
		title: i18n.t("filebrowser.title", "Filesystem"),
		body: '<div id="cms-file-browser"></div>',
		fullscreen: true,
		onOk: async (event) => {
			const selectedRow = document.querySelector("tr.table-active[data-cms-file-uri]:not([data-cms-file-directory])");
			if (selectedRow && state.options.onSelect) {
				const uri = selectedRow.getAttribute("data-cms-file-uri");
				const name = selectedRow.getAttribute("data-cms-file-name");
				state.options.onSelect({ uri, name });
			}
		},
		onShow: async () => {
			initFileBrowser();
		}
	});
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
			filenameHeader: i18n.t("filebrowser.filename", "Filename"),
			actionHeader: i18n.t("filebrowser.action", "Action"),
			actions: getActions(),
			asset: state.options.type === "assets"
		});
		makeDirectoriesClickable();
		if (state.options.onSelect) {
			makeFilesSelectable();
			enableRowSelection();
		}
		
		fileActions();
		initBootstrapTooltips();
		initDragAndDropUpload();
	}
};
const initBootstrapTooltips = () => {
	const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
	tooltipTriggerList.forEach((tooltipTriggerEl) => {
		new bootstrap.Tooltip(tooltipTriggerEl);
	});
};

const makeFilesSelectable = () => {
	const rows = document.querySelectorAll("tr[data-cms-file-uri]:not([data-cms-file-directory])");
	rows.forEach((row) => {
		row.addEventListener("dblclick", () => {
			const uri = row.getAttribute("data-cms-file-uri");
			const name = row.getAttribute("data-cms-file-name");
			if (state.options.onSelect) {
				state.options.onSelect({ uri, name });
			}
			state.modal.hide();
		});
	});
};
const enableRowSelection = () => {
	const rows = document.querySelectorAll("tr[data-cms-file-uri]:not([data-cms-file-directory])");
	rows.forEach((row) => {
		row.addEventListener("click", () => {
			rows.forEach(r => r.classList.remove("table-active"));
			row.classList.add("table-active");
		});
	});
};


const getActions = () => {
	const actions = []

	if (state.options.type === "content") {
		actions.push({
			id: "cms-filebrowser-action-createPage",
			name: i18n.t("filebrowser.create.page", "Create page")
		})
	}
	actions.push({
		id: "cms-filebrowser-action-createFile",
		name: i18n.t("filebrowser.create.file", "Create file")
	})
	actions.push({
		id: "cms-filebrowser-action-createFolder",
		name: i18n.t("filebrowser.create.folder", "Create folder")
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
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deletePage,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initFileBrowser(state.currentFolder);
				});
			} else if (action === "deleteFile") {
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deleteFile,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initFileBrowser(state.currentFolder);
				});
			} else if (action === "deleteFolder") {
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deleteFolder,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initFileBrowser(state.currentFolder);
				});
			} else if (action === "renameFile") {
				renameFileAction({
					state: state,
					getTargetFolder: getTargetFolder,
					filename: filename
				}).then(async () => {
					await initFileBrowser(state.currentFolder);
				});
			}
		});
	});

	document.getElementById("cms-filebrowser-action-createFolder").addEventListener("click", async (event) => {
		createFolderAction({
			state: state,
			getTargetFolder: getTargetFolder
		}).then(async () => {
			await initFileBrowser(state.currentFolder);
		});
	})
	document.getElementById("cms-filebrowser-action-createFile").addEventListener("click", async (event) => {
		createFileAction({
			state: state,
			getTargetFolder: getTargetFolder
		}).then(async () => {
			await initFileBrowser(state.currentFolder);
		});
	})
	if (document.getElementById("cms-filebrowser-action-createPage")) {
		document.getElementById("cms-filebrowser-action-createPage").addEventListener("click", async (event) => {
			createPageAction({
				getTargetFolder: getTargetFolder
			}).then(async () => {
				await initFileBrowser(state.currentFolder);
			});
		})
	}

	if (document.getElementById("cms-filebrowser-upload-button")) {
		document.getElementById("cms-filebrowser-upload-button").addEventListener("click", async (event) => {
			await handleFileUpload();
		});
	}

};

const getTargetFolder = () => {
	if (state.currentFolder.startsWith("/")) {
		return state.currentFolder.substring(1);
	}
	return state.currentFolder;
};

export { openFileBrowser, state };
