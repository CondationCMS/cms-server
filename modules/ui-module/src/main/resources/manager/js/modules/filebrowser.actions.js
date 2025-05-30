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

import { createFolder, createFile, renameFile } from '/manager/js/modules/rpc-files.js'
import { createPage } from '/manager/js/modules/rpc-page.js'
import { getPageTemplates } from '/manager/js/modules/ui-helpers.js'
import { i18n } from '/manager/js/modules/localization.js';
import { alertSelect, alertConfirm, alertPrompt } from '/manager/js/modules/alerts.js'
import { showToast } from '/manager/js/modules/toast.js'

export async function renameFileAction({ state, getTargetFolder, filename }) {
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
		}
	}
}

export async function deleteElementAction({ elementName, state, deleteFN, getTargetFolder }) {

	var confimred = await alertConfirm({
		title: i18n.t("ui.filebrowser.delete.confirm.title", "Are you sure?"),
		message: i18n.t("ui.filebrowser.delete.confirm.message", "You won't be able to revert this!"),
		confirmText: i18n.t("ui.filebrowser.delete.confirm.yes", "Yes, delete it!"),
		cancelText: i18n.t("ui.filebrowser.delete.confirm.no", "No, cancel!")
	});
	if (!confimred) {
		return;
	}

	var response = await deleteFN({
		uri: getTargetFolder(),
		name: elementName,
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
	}
}

export async function createFolderAction({ state, getTargetFolder }) {
	const folderName = await alertPrompt({
		title: i18n.t("ui.filebrowser.createFolder.title", "Create new folder"),
		label: i18n.t("ui.filebrowser.createFolder.label", "Folder name"),
		placeholder: i18n.t("ui.filebrowser.createFolder.placeholder", "New Folder")
	});
	if (folderName) {
		var response = await createFolder({
			uri: getTargetFolder(),
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
			showToast({
				title: 'Folder created',
				message: "Folder created successfully",
				type: 'info', // optional: info | success | warning | error
				timeout: 3000
			});
		}
	}
}

export async function createFileAction({ state, getTargetFolder }) {
	const fileName = await alertPrompt({
		title: i18n.t("ui.filebrowser.createFile.title", "Create new file"),
		label: i18n.t("ui.filebrowser.createFile.label", "File name"),
		placeholder: i18n.t("ui.filebrowser.createFile.placeholder", "New File")
	});
	if (fileName) {
		var response = await createFile({
			uri: getTargetFolder(),
			name: fileName,
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
			showToast({
				title: 'File created',
				message: "File created successfully",
				type: 'info', // optional: info | success | warning | error
				timeout: 3000
			});
		}
	}
}

export async function createPageAction({ getTargetFolder }) {
	const pageName = await alertPrompt({
		title: i18n.t("ui.filebrowser.createPage.title", "Create new page"),
		label: i18n.t("ui.filebrowser.createPage.label", "Page name"),
		placeholder: i18n.t("ui.filebrowser.createPage.placeholder", "New Page")
	});
	if (pageName) {
		const templateMap = getPageTemplates().reduce((acc, { name, template }) => {
			acc[template] = name;
			return acc;
		}, {});
		const selectedTemplate = await alertSelect({
			title: i18n.t("ui.filebrowser.createPage.selectTemplate.title", "Select a template"),
			placeholder: i18n.t("ui.filebrowser.createPage.selectTemplate.placeholder", "Select a template"),
			values: templateMap
		});

		if (selectedTemplate) {
			let response = await createPage({
				uri: getTargetFolder(),
				name: pageName,
				meta: {
					title: "New of: " + selectedTemplate,
					template: selectedTemplate,
					published: false
				}
			});
			if (response.error) {
				showToast({
					title: 'Error creating page',
					message: response.error.message,
					type: 'error', // optional: info | success | warning | error
					timeout: 3000
				});
			} else {
				showToast({
					title: 'Page created',
					message: "Page created successfully",
					type: 'info', // optional: info | success | warning | error
					timeout: 3000
				});
			}
		}
	}
}