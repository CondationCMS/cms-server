import frameMessenger from '../frameMessenger';
import { getPreviewFrame, getPreviewUrl } from '../preview.utils';
import { getContentNode } from '../rpc/rpc-content';

const executeImageForm = (payload: any) => {
    const cmd: any = {
        "module": window.manager.baseUrl + "/actions/media/edit-media-form",
        "function": "runAction",
        "parameters": {
            "editor": payload.editor,
            "attribute": payload.metaElement,
            "options": payload.options ? payload.options : {}
        }
    }
    if (payload.uri) {
        cmd.parameters.uri = payload.uri
    }
    executeScriptAction(cmd);
}

const executeImageSelect = (payload: any) => {
    const cmd: any = {
        "module": window.manager.baseUrl + "/actions/media/select-media",
        "function": "runAction",
        "parameters": {
            "options": payload.options ? payload.options : {}
        }
    }
    executeScriptAction(cmd);
}

const initMessageHandlers = () => {
    frameMessenger.on('preview:reload', (payload: any) => {

    });

    frameMessenger.on('edit', (payload: any) => {
        if (payload.element === "content") {
            var cmd: any = {
                "module": window.manager.baseUrl + "/actions/page/edit-content",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "options": payload.options ? payload.options : {}
                }
            }
            if (payload.uri) {
                cmd.parameters.uri = payload.uri
            }
            executeScriptAction(cmd)
        } else if (payload.element === "meta" && payload.editor === "form") {
            var cmd: any = {
                "module": window.manager.baseUrl + "/actions/page/edit-metaattribute-list",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "attributes": payload.metaElements,
                    "options": payload.options ? payload.options : {}
                }
            }
            if (payload.uri) {
                cmd.parameters.uri = payload.uri
            }
            executeScriptAction(cmd)
        } else if (payload.element === "image" && payload.editor === "form") {
            executeImageForm(payload);
        } else if (payload.element === "image" && payload.editor === "select") {
            executeImageSelect(payload);
        } else if (payload.element === "image" && payload.editor === "focal-point") {
            var cmd: any = {
                "module": window.manager.baseUrl + "/actions/media/edit-focal-point",
                "function": "runAction",
                "parameters": {
                    "options": payload.options ? payload.options : {}
                }
            }
            if (payload.uri) {
                cmd.parameters.uri = payload.uri
            }
            executeScriptAction(cmd);
        } else if (payload.element === "meta") {
            var cmd : any = {
                "module": window.manager.baseUrl + "/actions/page/edit-metaattribute",
                "function": "runAction",
                "parameters": {
                    "editor": payload.editor,
                    "attribute": payload.metaElement,
                    "options": payload.options ? payload.options : {}
                }
            }
            if (payload.uri) {
                cmd.parameters.uri = payload.uri
            }
            executeScriptAction(cmd)
        }
    });
    frameMessenger.on('edit-sections', (payload) => {
        var cmd : any= {
            "module": window.manager.baseUrl + "/actions/page/edit-sections",
            "function": "runAction",
            "parameters": {
                "sectionName": payload.sectionName
            }
        }
        if (payload.uri) {
            cmd.parameters.uri = payload.uri
        }
        executeScriptAction(cmd)
    });

    frameMessenger.on('add-section', (payload: any) => {
        var cmd : any = {
            "module": window.manager.baseUrl + "/actions/page/add-section",
            "function": "runAction",
            "parameters": {
                "sectionName": payload.sectionName
            }
        }
        executeScriptAction(cmd)
    });

    frameMessenger.on('delete-section', (payload : any) => {
        var cmd :any = {
            "module": window.manager.baseUrl + "/actions/page/delete-section",
            "function": "runAction",
            "parameters": {
                "sectionUri": payload.sectionUri
            }
        }
        executeScriptAction(cmd)
    });

    frameMessenger.on('shortkeys', (payload : any) => {
        const ninja: any = document.querySelector('ninja-keys');
        ninja.open();
    });

    frameMessenger.on("section-set-published", (payload: any) => {
        var cmd :any = {
            "module": window.manager.baseUrl + "/actions/page/section-set-published",
            "function": "runAction",
            "parameters": {
                "sectionUri": payload.sectionUri,
                "published": payload.published
            }
        }
        executeScriptAction(cmd)
    });
    frameMessenger.on('getContentNode', async (payload: any) => {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        })
        var message : any = {
            "type": "getContentNodeResponse",
            "payload": {
                "contentNode": contentNode.result
            }
        }
        var previewFrame = getPreviewFrame() as any
        frameMessenger.send(previewFrame.contentWindow, message);
    });
}

export { initMessageHandlers };