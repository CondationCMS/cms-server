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
import { loadPreview } from './preview.utils.js'
import { i18n } from './localization.js';

import { renameFileAction, deleteElementAction, createFolderAction, createFileAction, createPageAction, createPageActionOfContentType } from './filebrowser.actions.js'
import { initDragAndDropUpload, handleFileUpload } from './filebrowser.upload.js';
import { EventBus } from './event-bus.js';
import { filebrowserTemplate } from './filebrowser.template.js';
import { getPageTemplates } from './rpc/rpc-manager.js';

const defaultOptions = {
	validate: () => true,
	uri: "",
	onSelect: null
};

const state = {
	options: null,
	currentFolder: ""
};


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
		fileBrowserElement.innerHTML = filebrowserTemplate({
			files: contentFiles.result.files,
			filenameHeader: i18n.t("filebrowser.filename", "Filename"),
			actionHeader: i18n.t("filebrowser.action", "Action"),
			actions: getActions(),
			asset: state.options.type === "assets",
			pageContentTypes : (await getPageTemplates()).result
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

	/*
	if (state.options.type === "content") {
		actions.push({
			id: "cms-filebrowser-action-createPage",
			name: i18n.t("filebrowser.create.page", "Create page")
		})
	}
	*/
	/*
	actions.push({
		id: "cms-filebrowser-action-createFile",
		name: i18n.t("filebrowser.create.file", "Create file")
	})
	*/
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
	/*
	document.getElementById("cms-filebrowser-action-createFile").addEventListener("click", async (event) => {
		createFileAction({
			state: state,
			getTargetFolder: getTargetFolder
		}).then(async () => {
			await initFileBrowser(state.currentFolder);
		});
	})
		*/
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

	document.querySelectorAll("[data-cms-filbrowser-ct-action='create']").forEach((element) => {
		element.addEventListener("click", async (event) => {
			event.preventDefault();
			const contentType = element.getAttribute("data-cms-contenttype");
			if (contentType) {
				createPageActionOfContentType({
					getTargetFolder: getTargetFolder,
					contentType: contentType
				}).then(async () => {
					await initFileBrowser(state.currentFolder);
				});
			}
		});
	});

};

const getTargetFolder = () => {
	if (state.currentFolder.startsWith("/")) {
		return state.currentFolder.substring(1);
	}
	return state.currentFolder;
};

export { openFileBrowser, state };
