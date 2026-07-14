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
import { listFiles } from '@cms/modules/rpc/rpc-files.js';
import { createPage } from '@cms/modules/rpc/rpc-page';
import { openModal } from '@cms/modules/modal.js';
import { i18n } from '@cms/modules/localization.js';
import { alertPrompt } from '@cms/modules/alerts.js';
import { showToast } from '@cms/modules/toast.js';
import { mediabrowserTemplate } from '@cms/modules/media/mediabrowser.template.js';
import { createFolderAction } from '@cms/modules/media/mediabrowser.actions.js';
const defaultOptions = {
    uri: "",
    template: null,
    contentType: null,
    onCreate: null,
    fullscreen: true,
    title: i18n.t("filebrowser.create.title", "Create node"),
    type: null,
    siteId: null
};
const state = {
    options: null,
    currentFolder: "",
    modal: null
};
const openCreateContentBrowser = async (params) => {
    state.options = { ...defaultOptions, ...params };
    state.modal = openModal({
        title: state.options.title,
        body: '<div id="cms-create-content-browser"></div>',
        fullscreen: state.options.fullscreen,
        onOk: async () => {
            const fileName = await alertPrompt({
                title: i18n.t("filebrowser.createFile.title", "Create new node"),
                label: i18n.t("filebrowser.createFile.label", "Node name"),
                placeholder: i18n.t("filebrowser.createFile.placeholder", "New Node")
            });
            if (!fileName)
                return;
            const extraOptions = {};
            if (state.options.siteId) {
                extraOptions.siteId = state.options.siteId;
            }
            try {
                const response = await createPage({
                    uri: getTargetFolder(),
                    name: fileName,
                    contentType: state.options.contentType,
                    type: state.options.type,
                    ...extraOptions
                });
                showToast({
                    title: i18n.t("filebrowser.createFile.success.title", 'File created'),
                    message: i18n.t("filebrowser.createFile.success.message", "File created successfully"),
                    type: 'success',
                    timeout: 3000
                });
                if (state.options.onCreate) {
                    state.options.onCreate({
                        uri: response.result.uri,
                        name: fileName
                    });
                }
            }
            catch (e) {
                showToast({
                    title: i18n.t("filebrowser.createFile.error.title", 'Error creating file'),
                    message: e.message,
                    type: 'error',
                    timeout: 3000
                });
            }
        },
        onShow: async () => {
            initBrowser(state.options.uri);
        }
    });
};
const initBrowser = async (uri) => {
    state.currentFolder = uri ?? "";
    const options = {
        type: state.options.type,
        uri: state.currentFolder
    };
    if (state.options.siteId) {
        options.siteId = state.options.siteId;
    }
    const contentFiles = await listFiles(options);
    const files = contentFiles.result.files;
    const directories = files.filter(f => f.directory);
    const browserElement = document.getElementById("cms-create-content-browser");
    if (!browserElement)
        return;
    browserElement.innerHTML = mediabrowserTemplate({
        files: directories,
        filenameHeader: i18n.t("filebrowser.filename", "Filename"),
        actionHeader: i18n.t("filebrowser.action", "Action"),
        actions: [{
                id: "cms-create-content-action-createFolder",
                name: i18n.t("filebrowser.create.folder", "Create folder")
            }],
        asset: false,
        pageContentTypes: []
    });
    makeDirectoriesClickable();
    wireActions();
    initBootstrapTooltips();
};
const makeDirectoriesClickable = () => {
    document.querySelectorAll("[data-cms-file-directory]").forEach((element) => {
        element.addEventListener("dblclick", (event) => {
            event.stopPropagation();
            const directory = element.getAttribute("data-cms-file-uri");
            if (directory) {
                initBrowser(directory);
            }
        });
    });
};
const wireActions = () => {
    const createFolderBtn = document.getElementById("cms-create-content-action-createFolder");
    if (createFolderBtn) {
        createFolderBtn.addEventListener("click", async () => {
            await createFolderAction({ state, getTargetFolder });
            await initBrowser(state.currentFolder);
        });
    }
};
const initBootstrapTooltips = () => {
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach((el) => {
        new bootstrap.Tooltip(el);
    });
};
const getTargetFolder = () => {
    if (state.currentFolder.startsWith("/")) {
        return state.currentFolder.substring(1);
    }
    return state.currentFolder;
};
export { openCreateContentBrowser };
