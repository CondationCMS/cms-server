/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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
import { executeScriptAction } from '@cms/js/manager-globals.js';
import frameMessenger from '@cms/modules/frameMessenger.js';
import { getPreviewFrame, getPreviewUrl } from '@cms/modules/preview.utils.js';
import { getContentNode } from '@cms/modules/rpc/rpc-content.js';
const executeImageForm = (payload) => {
    const cmd = {
        "module": window.manager.baseUrl + "/actions/media/edit-media-form",
        "function": "runAction",
        "parameters": {
            "editor": payload.editor,
            "attribute": payload.metaElement,
            "options": payload.options ? payload.options : {}
        }
    };
    if (payload.uri) {
        cmd.parameters.uri = payload.uri;
    }
    executeScriptAction(cmd);
};
const executeImageSelect = (payload) => {
    const cmd = {
        "module": window.manager.baseUrl + "/actions/media/select-media",
        "function": "runAction",
        "parameters": {
            "options": payload.options ? payload.options : {}
        }
    };
    executeScriptAction(cmd);
};
const initMessageHandlers = () => {
    frameMessenger.on('preview:reload', (payload) => {
    });
    frameMessenger.on('edit', (payload) => {
        if (payload.element === "content") {
            var cmd = {
                "module": window.manager.baseUrl + "/actions/page/edit-content",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "options": payload.options ? payload.options : {}
                }
            };
            if (payload.uri) {
                cmd.parameters.uri = payload.uri;
            }
            executeScriptAction(cmd);
        }
        else if (payload.element === "meta" && payload.editor === "form") {
            var cmd = {
                "module": window.manager.baseUrl + "/actions/page/edit-metaattribute-form",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "attributes": payload.metaElements,
                    "options": payload.options ? payload.options : {},
                    "form": payload.form,
                    "type": payload.type
                }
            };
            if (payload.uri) {
                cmd.parameters.uri = payload.uri;
            }
            executeScriptAction(cmd);
        }
        else if (payload.element === "image" && payload.editor === "form") {
            executeImageForm(payload);
        }
        else if (payload.element === "image" && payload.editor === "select") {
            executeImageSelect(payload);
        }
        else if (payload.element === "image" && payload.editor === "focal-point") {
            var cmd = {
                "module": window.manager.baseUrl + "/actions/media/edit-focal-point",
                "function": "runAction",
                "parameters": {
                    "options": payload.options ? payload.options : {}
                }
            };
            if (payload.uri) {
                cmd.parameters.uri = payload.uri;
            }
            executeScriptAction(cmd);
        }
        else if (payload.element === "meta") {
            var cmd = {
                "module": window.manager.baseUrl + "/actions/page/edit-metaattribute",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "attribute": payload.metaElement,
                    "options": payload.options ? payload.options : {}
                }
            };
            if (payload.uri) {
                cmd.parameters.uri = payload.uri;
            }
            executeScriptAction(cmd);
        }
    });
    frameMessenger.on('edit-sections', (payload) => {
        var cmd = {
            "module": window.manager.baseUrl + "/actions/page/edit-sections",
            "function": "runAction",
            "parameters": {
                "sectionName": payload.sectionName
            }
        };
        if (payload.uri) {
            cmd.parameters.uri = payload.uri;
        }
        executeScriptAction(cmd);
    });
    frameMessenger.on('add-section', (payload) => {
        var cmd = {
            "module": window.manager.baseUrl + "/actions/page/add-section",
            "function": "runAction",
            "parameters": {
                "sectionName": payload.sectionName
            }
        };
        executeScriptAction(cmd);
    });
    frameMessenger.on('delete-section', (payload) => {
        var cmd = {
            "module": window.manager.baseUrl + "/actions/page/delete-section",
            "function": "runAction",
            "parameters": {
                "sectionUri": payload.sectionUri
            }
        };
        executeScriptAction(cmd);
    });
    frameMessenger.on('shortkeys', (payload) => {
        const ninja = document.querySelector('ninja-keys');
        ninja.open();
    });
    frameMessenger.on("section-set-published", (payload) => {
        var cmd = {
            "module": window.manager.baseUrl + "/actions/page/section-set-published",
            "function": "runAction",
            "parameters": {
                "sectionUri": payload.sectionUri,
                "published": payload.published
            }
        };
        executeScriptAction(cmd);
    });
    frameMessenger.on('getContentNode', async (payload) => {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        });
        var message = {
            "type": "getContentNodeResponse",
            "payload": {
                "contentNode": contentNode.result
            }
        };
        var previewFrame = getPreviewFrame();
        frameMessenger.send(previewFrame.contentWindow, message);
    });
};
export { initMessageHandlers };
