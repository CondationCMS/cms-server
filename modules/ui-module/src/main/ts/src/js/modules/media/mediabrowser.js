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

import { listFiles, deleteFile, deleteFolder, } from '@cms/modules/rpc/rpc-files.js'
import { deletePage } from '@cms/modules/rpc/rpc-page.js'
import { openModal } from '@cms/modules/modal.js'
import { loadPreview } from '@cms/modules/preview.utils.js'
import { i18n } from '@cms/modules/localization.js';

import { renameFileAction, deleteElementAction, createFolderAction, createFileAction, createPageActionOfContentType } from '@cms/modules/media/mediabrowser.actions.js'
import { initDragAndDropUpload, handleFileUpload } from '@cms/modules/media/mediabrowser.upload.js';
import { EventBus } from '@cms/modules/event-bus.js';
import { mediabrowserTemplate } from '@cms/modules/media/mediabrowser.template.js';
import { getPageTemplates } from '@cms/modules/rpc/rpc-manager.js';
import { showToast } from '@cms/modules/toast.js';

const defaultOptions = {
	validate: () => true,
	uri: "",
	onSelect: null,
	fullscreen: true,
	title: i18n.t("mediabrowser.title", "Media Browser"),
	filter : (file) => {
		return true; // Default filter allows all files
	}
};

const state = {
	options: null,
	currentFolder: ""
};

let cachedPageTemplates = null;

EventBus.on("upload:success", (folder) => {
	initMediaBrowser(state.currentFolder);
});

const openMediaBrowser = async (optionsParam) => {
	state.options = {
		...defaultOptions,
		...optionsParam
	};

	state.modal = openModal({
		title: state.options.title,
		body: '<div id="cms-file-browser"></div>',
		fullscreen: state.options.fullscreen,
		onOk: async (event) => {
			const selectedRow = document.querySelector(".cms-media-card.selected[data-cms-file-uri]:not([data-cms-file-directory])");
			if (selectedRow && state.options.onSelect) {
				const uri = selectedRow.getAttribute("data-cms-file-uri");
				const name = selectedRow.getAttribute("data-cms-file-name");
				state.options.onSelect({ uri, name });
			}
		},
		onShow: async () => {
			initMediaBrowser(state.options.uri);
		}
	});
};

const initMediaBrowser = async (uri) => {
	if (!state.options) return;
	state.currentFolder = uri ? uri : "";

	var options = {
		type: state.options.type,
		uri: state.currentFolder
	}
	if (state.options.siteId) {
		options.siteId = state.options.siteId;
	}

	const contentFiles = await listFiles(options);

	var files = contentFiles.result.files;
	if (state.options.filter) {
		files = files.filter(state.options.filter);
	}

	const fileBrowserElement = document.getElementById("cms-file-browser");
	if (fileBrowserElement) {
		fileBrowserElement.innerHTML = mediabrowserTemplate({
			files: files,
			filenameHeader: i18n.t("filebrowser.filename", "Filename"),
			actionHeader: i18n.t("filebrowser.action", "Action"),
			actions: getActions(),
			asset: state.options.type === "assets",
			pageContentTypes: (cachedPageTemplates ??= (await getPageTemplates()).result)
		});
		makeDirectoriesClickable();
		if (state.options.onSelect) {
			makeFilesSelectableAndSelectable();
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

const makeFilesSelectableAndSelectable = () => {
	const rows = document.querySelectorAll(".cms-media-card[data-cms-file-uri]:not([data-cms-file-directory])");
	rows.forEach((row) => {
		row.addEventListener("dblclick", () => {
			const uri = row.getAttribute("data-cms-file-uri");
			const name = row.getAttribute("data-cms-file-name");
			if (state.options.onSelect) {
				state.options.onSelect({ uri, name });
			}
			state.modal.hide();
		});
		row.addEventListener("click", () => {
			rows.forEach(r => r.classList.remove("selected"));
			row.classList.add("selected");
		});
	});
};


const getActions = () => [
	{
		id: "cms-filebrowser-action-createFolder",
		name: i18n.t("filebrowser.create.folder", "Create folder")
	}
]

const makeDirectoriesClickable = () => {
	const elements = document.querySelectorAll("[data-cms-file-directory]");
	elements.forEach((element) => {
		element.addEventListener("dblclick", (event) => {
			event.stopPropagation();
			const directory = element.getAttribute("data-cms-file-uri");
			if (directory) {
				initMediaBrowser(directory);
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
			} else if (action === "copyUrl") {
				navigator.clipboard.writeText(uri).then(
					() => {
						showToast({
							title: i18n.t('filebrowser.actions.url.copy.title', "URL copied"),
							message: i18n.t('filebrowser.actions.url.copy.message', "URL copied to clipboard"),
							type: 'success',
							timeout: 3000
						});
					},
					() => {
						showToast({
							title: i18n.t('filebrowser.actions.url.copy.title.error', "Error copying URL"),
							message: i18n.t('filebrowser.actions.url.copy.message.error', "Failed to copy URL"),
							type: 'error',
							timeout: 3000
						});
					},
  				);
			} else if (action === "deletePage") {
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deletePage,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initMediaBrowser(state.currentFolder);
				});
			} else if (action === "deleteFile") {
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deleteFile,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initMediaBrowser(state.currentFolder);
				});
			} else if (action === "deleteFolder") {
				deleteElementAction({
					elementName: filename,
					state: state,
					deleteFN: deleteFolder,
					getTargetFolder: getTargetFolder
				}).then(async () => {
					await initMediaBrowser(state.currentFolder);
				});
			} else if (action === "renameFile") {
				renameFileAction({
					state: state,
					getTargetFolder: getTargetFolder,
					filename: filename
				}).then(async () => {
					await initMediaBrowser(state.currentFolder);
				});
			}
		});
	});

	const createFolderBtn = document.getElementById("cms-filebrowser-action-createFolder");
	if (createFolderBtn) {
		createFolderBtn.addEventListener("click", async (event) => {
			createFolderAction({
				state: state,
				getTargetFolder: getTargetFolder
			}).then(async () => {
				await initMediaBrowser(state.currentFolder);
			});
		});
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
					state: state,
					getTargetFolder: getTargetFolder,
					contentType: contentType
				}).then(async () => {
					await initMediaBrowser(state.currentFolder);
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

export { openMediaBrowser, state };
