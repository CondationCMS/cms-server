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
import { createFolder, createFile, renameFile } from './rpc/rpc-files.js';
import { createPage } from './rpc/rpc-page.js';
import { i18n } from './localization.js';
import { alertSelect, alertConfirm, alertPrompt } from './alerts.js';
import { showToast } from './toast.js';
import { getPageTemplates } from './rpc/rpc-manager.js';
export async function renameFileAction({ state, getTargetFolder, filename }) {
    const newName = await alertPrompt({
        title: i18n.t("filebrowser.rename.title", "Rename file"),
        label: i18n.t("filebrowser.rename.label", "New name"),
        placeholder: filename
    });
    var extraOptions = {};
    if (state.options.siteId) {
        extraOptions.siteId = state.options.siteId;
    }
    if (newName) {
        var response = await renameFile({
            uri: getTargetFolder(),
            name: filename,
            newName: newName,
            type: state.options.type,
            ...extraOptions
        });
        if (response.error) {
            showToast({
                title: i18n.t("filebrowser.rename.error.title", 'Error renaming file'),
                message: response.error.message,
                type: 'error',
                timeout: 3000
            });
        }
        else {
            showToast({
                title: i18n.t("filebrowser.rename.success.title", 'File renamed'),
                message: i18n.t("filebrowser.rename.success.message", "File renamed successfully"),
                type: 'info',
                timeout: 3000
            });
        }
    }
}
export async function deleteElementAction({ elementName, state, deleteFN, getTargetFolder }) {
    var confimred = await alertConfirm({
        title: i18n.t("filebrowser.delete.confirm.title", "Are you sure?"),
        message: i18n.t("filebrowser.delete.confirm.message", "You won't be able to revert this!"),
        confirmText: i18n.t("filebrowser.delete.confirm.yes", "Yes, delete it!"),
        cancelText: i18n.t("filebrowser.delete.confirm.no", "No, cancel!")
    });
    if (!confimred) {
        return;
    }
    var extraOptions = {};
    if (state.options.siteId) {
        extraOptions.siteId = state.options.siteId;
    }
    var response = await deleteFN({
        uri: getTargetFolder(),
        name: elementName,
        type: state.options.type,
        ...extraOptions
    });
    if (response.error) {
        showToast({
            title: 'Error deleting',
            message: response.error.message,
            type: 'error', // optional: info | success | warning | error
            timeout: 3000
        });
    }
    else {
        showToast({
            title: 'Element deleted',
            message: "Element deleted",
            type: 'success', // optional: info | success | warning | error
            timeout: 3000
        });
    }
}
export async function createFolderAction({ state, getTargetFolder }) {
    const folderName = await alertPrompt({
        title: i18n.t("filebrowser.createFolder.title", "Create new folder"),
        label: i18n.t("filebrowser.createFolder.label", "Folder name"),
        placeholder: i18n.t("filebrowser.createFolder.placeholder", "New Folder")
    });
    if (folderName) {
        var extraOptions = {};
        if (state.options.siteId) {
            extraOptions.siteId = state.options.siteId;
        }
        var response = await createFolder({
            uri: getTargetFolder(),
            name: folderName,
            type: state.options.type,
            ...extraOptions
        });
        if (response.error) {
            showToast({
                title: i18n.t("filebrowser.createFolder.error.title", 'Error creating folder'),
                message: response.error.message,
                type: 'error', // optional: info | success | warning | error
                timeout: 3000
            });
        }
        else {
            showToast({
                title: i18n.t("filebrowser.createFolder.success.title", 'Folder created'),
                message: i18n.t("filebrowser.createFolder.success.message", "Folder created successfully"),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
        }
    }
}
export async function createFileAction({ state, getTargetFolder }) {
    const fileName = await alertPrompt({
        title: i18n.t("filebrowser.createFile.title", "Create new file"),
        label: i18n.t("filebrowser.createFile.label", "File name"),
        placeholder: i18n.t("filebrowser.createFile.placeholder", "New File")
    });
    if (fileName) {
        var extraOptions = {};
        if (state.options.siteId) {
            extraOptions.siteId = state.options.siteId;
        }
        var response = await createFile({
            uri: getTargetFolder(),
            name: fileName,
            type: state.options.type,
            ...extraOptions
        });
        if (response.error) {
            showToast({
                title: i18n.t("filebrowser.createFile.error.title", 'Error creating file'),
                message: response.error.message,
                type: 'error', // optional: info | success | warning | error
                timeout: 3000
            });
        }
        else {
            showToast({
                title: i18n.t("filebrowser.createFile.success.title", 'File created'),
                message: i18n.t("filebrowser.createFile.success.message", "File created successfully"),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
        }
    }
}
export async function createPageActionOfContentType({ state, getTargetFolder, contentType }) {
    const pageName = await alertPrompt({
        title: i18n.t("filebrowser.createPage.title", "Create new page"),
        label: i18n.t("filebrowser.createPage.label", "Page name"),
        placeholder: i18n.t("filebrowser.createPage.placeholder", "New Page")
    });
    if (pageName) {
        if (contentType) {
            var extraOptions = {};
            if (state.options.siteId) {
                extraOptions.siteId = state.options.siteId;
            }
            let response = await createPage({
                uri: getTargetFolder(),
                name: pageName,
                contentType: contentType,
                ...extraOptions
            });
            if (response.error) {
                showToast({
                    title: i18n.t("filebrowser.createPage.error.title", 'Error creating page'),
                    message: response.error.message,
                    type: 'error', // optional: info | success | warning | error
                    timeout: 3000
                });
            }
            else {
                showToast({
                    title: i18n.t("filebrowser.createPage.success.title", 'Page created'),
                    message: i18n.t("filebrowser.createPage.success.message", 'Page successfuly created'),
                    type: 'success', // optional: info | success | warning | error
                    timeout: 3000
                });
            }
        }
    }
}
