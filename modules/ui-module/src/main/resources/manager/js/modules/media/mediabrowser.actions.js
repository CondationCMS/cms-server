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
import { createFolder, createFile } from '@cms/modules/rpc/rpc-files.js';
import { renameMedia } from '@cms/modules/rpc/rpc-media';
import { createPage } from '@cms/modules/rpc/rpc-page.js';
import { i18n } from '@cms/modules/localization.js';
import { alertConfirm, alertPrompt } from '@cms/modules/alerts.js';
import { showToast } from '@cms/modules/toast.js';
export async function renameMediaAction({ state, getTargetFolder, filename }) {
    const newName = await alertPrompt({
        title: i18n.t("mediabrowser.rename.title", "Rename media"),
        label: i18n.t("mediabrowser.rename.label", "New name"),
        placeholder: filename
    });
    var extraOptions = {};
    if (state.options.siteId) {
        extraOptions.siteId = state.options.siteId;
    }
    if (newName) {
        try {
            await renameMedia({
                uri: getTargetFolder(),
                name: filename,
                newName: newName,
                ...extraOptions
            });
            showToast({
                title: i18n.t("mediabrowser.rename.success.title", 'Media renamed'),
                message: i18n.t("mediabrowser.rename.success.message", "Media renamed successfully"),
                type: 'info',
                timeout: 3000
            });
        }
        catch (e) {
            showToast({
                title: i18n.t("mediabrowser.rename.error.title", 'Error renaming media'),
                message: e.message,
                type: 'error',
                timeout: 3000
            });
        }
    }
}
export async function deleteElementAction({ elementName, state, deleteFN, getTargetFolder }) {
    var confimred = await alertConfirm({
        title: i18n.t("mediabrowser.delete.confirm.title", "Are you sure?"),
        message: i18n.t("mediabrowser.delete.confirm.message", "You won't be able to revert this!"),
        confirmText: i18n.t("mediabrowser.delete.confirm.yes", "Yes, delete it!"),
        cancelText: i18n.t("mediabrowser.delete.confirm.no", "No, cancel!")
    });
    if (!confimred) {
        return;
    }
    var extraOptions = {};
    if (state.options.siteId) {
        extraOptions.siteId = state.options.siteId;
    }
    try {
        await deleteFN({
            uri: getTargetFolder(),
            name: elementName,
            type: state.options.type,
            ...extraOptions
        });
        showToast({
            title: 'Element deleted',
            message: "Element deleted",
            type: 'success', // optional: info | success | warning | error
            timeout: 3000
        });
    }
    catch (e) {
        showToast({
            title: 'Error deleting',
            message: e.message,
            type: 'error', // optional: info | success | warning | error
            timeout: 3000
        });
    }
}
export async function createFolderAction({ state, getTargetFolder }) {
    const folderName = await alertPrompt({
        title: i18n.t("mediabrowser.createFolder.title", "Create new folder"),
        label: i18n.t("mediabrowser.createFolder.label", "Folder name"),
        placeholder: i18n.t("mediabrowser.createFolder.placeholder", "New Folder")
    });
    if (folderName) {
        var extraOptions = {};
        if (state.options.siteId) {
            extraOptions.siteId = state.options.siteId;
        }
        try {
            await createFolder({
                uri: getTargetFolder(),
                name: folderName,
                type: state.options.type,
                ...extraOptions
            });
            showToast({
                title: i18n.t("mediabrowser.createFolder.success.title", 'Folder created'),
                message: i18n.t("mediabrowser.createFolder.success.message", "Folder created successfully"),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
        }
        catch (e) {
            showToast({
                title: i18n.t("mediabrowser.createFolder.error.title", 'Error creating folder'),
                message: e.message,
                type: 'error', // optional: info | success | warning | error
                timeout: 3000
            });
        }
    }
}
