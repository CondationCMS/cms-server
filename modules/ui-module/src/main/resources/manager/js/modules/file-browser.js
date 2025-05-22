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

import { listFiles } from '/manager/js/modules/rpc-files.js'
import { openModal } from '/manager/js/modules/modal.js'
import Handlebars from 'https://cdn.jsdelivr.net/npm/handlebars@latest/+esm';
import { loadPreview } from '/manager/js/modules/ui-helpers.js'
import { i18n } from '/manager/js/modules/localization.js';

const defaultOptions = {
	validate: () => true
};

const state = {
	options: null,
	currentUri: null
};

const template = Handlebars.compile(`
	<div>
		<div class="dropdown">
			<button class="btn btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
				Actions
			</button>
			<ul class="dropdown-menu">
				<li><a class="dropdown-item" href="#">Create File</a></li>
				<li><a class="dropdown-item" href="#" id="cms-filebrowser-action-createFolder">Create Folder</a></li>
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
						
					{{else}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="open"><i class="bi bi-file-arrow-up"></i></button>
					{{/if}}
				</td>
			<tr>
		{{/each}}
		</tbody>
	</table>
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

const handleCreateFolder = async (folderName) => {
	console.log("Creating folder:", folderName);

	// Beispiel: Hier könntest du z.B. einen RPC aufrufen
	// await createFolderRpc({ uri: state.currentUri, name: folderName });

	await initFileBrowser(state.currentUri);
};

const insertFolderInputRow = () => {
	const tableBody = document.getElementById("cms-filebrowser-files");
	if (!tableBody) return;

	// Prüfe, ob bereits eine Eingabezeile existiert
	if (document.getElementById("cms-new-folder-row")) return;

	const row = document.createElement("tr");
	row.id = "cms-new-folder-row";
	row.innerHTML = `
		<th scope="row"><i class="bi bi-folder-plus"></i></th>
		<td><input id="cms-new-folder-input" class="form-control" type="text" placeholder="${i18n.t("ui.filebrowser.enter.foldername", "Enter folder name")}" /></td>
		<td></td>
	`;
	tableBody.prepend(row);

	const input = document.getElementById("cms-new-folder-input");
	input.focus();

	input.addEventListener("keydown", (event) => {
		if (event.key === "Enter") {
			const folderName = input.value.trim();
			if (folderName.length > 0) {
				handleCreateFolder(folderName);
			} else {
				removeFolderInputRow();
			}
		} else if (event.key === "Escape") {
			removeFolderInputRow();
		}
	});
};

const removeFolderInputRow = () => {
	const existingRow = document.getElementById("cms-new-folder-row");
	if (existingRow) {
		existingRow.remove();
	}
};

const createFolder = async () => {
	insertFolderInputRow();
};

const initFileBrowser = async (uri) => {
	state.currentUri = uri ? uri : null;

	const contentFiles = await listFiles({
		type: state.options.type,
		uri: state.currentUri
	});

	const fileBrowserElement = document.getElementById("cms-file-browser");
	if (fileBrowserElement) {
		fileBrowserElement.innerHTML = template({
			files: contentFiles.result.files,
			filenameHeader: i18n.t("ui.filebrowser.filename", "Filename"),
			actionHeader: i18n.t("ui.filebrowser.action", "Action")
		});
		makeDirectoriesClickable();
		fileActions();
	}
};

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
		element.addEventListener("click", (event) => {
			event.stopPropagation();
			const uri = element.getAttribute("data-cms-file-uri");
			const action = element.getAttribute("data-cms-file-action");

			if (action === "open") {
				loadPreview(uri);
				state.modal.hide();
			}
		});
	});

	document.getElementById("cms-filebrowser-action-createFolder").addEventListener("click", async (event) => {
		await createFolder()
	})
};

export { openFileBrowser };
