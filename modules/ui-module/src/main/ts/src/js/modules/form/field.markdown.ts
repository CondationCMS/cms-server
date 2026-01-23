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
import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js"
import { getMediaFormats, getTagNames } from "@cms/modules/rpc/rpc-manager.js";
import { openFileBrowser } from "@cms/modules/filebrowser.js";
import { alertSelect } from "@cms/modules/alerts.js";
import { FieldOptions, FormContext, FormField } from "@cms/modules/form/forms.js";
import { patchPathWithContext } from "@cms/js/manager-globals";

// Am Anfang der Datei
declare global {
    interface HTMLInputElement {
        cherryEditor?: any;
    }
}

export interface MarkdownFieldOptions extends FieldOptions {
	placeholder?: string;
	height?: string;
}

const createMarkdownField = (options: MarkdownFieldOptions, value: string = '') => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	return `
		<div class="mb-3 cms-form-field flex-grow-1" data-cms-form-field-type="markdown">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			<div id="${id}" class="cherry-editor-container" style="min-height: ${options.height || '300px'}; border: 1px solid #ccc;"></div>
			<input type="hidden" name="${options.name}" data-cherry-id="${id}" data-initial-value="${encodeURIComponent(value)}">
		</div>
	`;
};

const getData = (context : FormContext) => {
    const data = {};

    const editorInputs = context.formElement.querySelectorAll('[data-cms-form-field-type="markdown"] input');
    
    editorInputs.forEach((input: HTMLInputElement) => {
        const editor = (input as any).cherryEditor;
        
        if (editor && editor.getMarkdown) {
            data[input.name] = {
                type: "markdown",
                value: editor.getMarkdown()
            }
        } else {
            // Fallback: leerer Wert, falls Editor nicht initialisiert
            data[input.name] = {
                type: "markdown",
                value: ""
            }
        }
    });
    
    return data;
};


const init = async (context : FormContext) => {

	const cmsTagsMenu = await buildCmsTagsMenu();

	const editorInputs = context.formElement.querySelectorAll('[data-cms-form-field-type="markdown"] input');
	editorInputs.forEach((input: HTMLInputElement) => {
		const containerId = input.dataset.cherryId;
		const initialValue = decodeURIComponent(input.dataset.initialValue || "");

		const editor = new window.Cherry({
			id: containerId,
			value: initialValue,
			height: '100%',
			locale: 'en_US',
			editor: {
				defaultModel: 'editOnly'
			},
			toolbars: {
				toolbar: [
					'bold',
					'italic',
					'strikethrough',
					'|',
					'color',
					'header',
					'|',
					'list',
					'code',
					'|',
					'cmsImageSelection',
					'cmsTagsMenu',
				],
				bubble: ['bold', 'italic', 'underline', 'strikethrough', 'sub', 'sup', 'quote', '|', 'size', 'color'], // array or false
				float: ['h1', 'h2', 'h3', '|', 'checklist', 'table', 'code'],
				customMenu: {
					cmsTagsMenu: cmsTagsMenu,
					cmsImageSelection: cmsImageSelection
				},
			}
		});

		(input as any).cherryEditor = editor;
	});
};

export const MarkdownField = {
	markup: createMarkdownField,
	init: init,
	data: getData
} as FormField;

// Helper-Funktion
const getEditorFromEvent = (event: any): any => {
    const editorContainer = event.target.closest('.cherry-editor-container');
    if (!editorContainer) return null;

    const input = document.querySelector(
        `input[data-cherry-id="${editorContainer.id}"]`
    ) as HTMLInputElement;
    
    return input ? (input as any).cherryEditor : null;
};

const buildCmsTagsMenu = async () => {
	const response = await getTagNames({});
	const tagNames = response.result || [];

	const submenuConfig = tagNames.map(tag => ({
		name: tag.charAt(0).toUpperCase() + tag.slice(1),
		value: tag,
		noIcon: true,
		onclick: (event: any) => {
			const editor = getEditorFromEvent(event);
			if (editor) {
				editor.toolbar.menus.hooks["cmsTagsMenu"].fire(null, tag);
			}
		}
	}));

	return window.Cherry.createMenuHook("CMS-Tags", {
		title: "CMS Tags",
		onClick: (selection, tag) => {
			return `[[${tag}]]${selection || ""}[[/${tag}]]`;
		},
		subMenuConfig: submenuConfig
	});
};

const cmsImageSelection = window.Cherry.createMenuHook("Image", {
	iconName: "image",
	title: "Image",
	onClick: (
		selection: any,
		name: any,
		event: any) => {

		openFileBrowser({
			type: "assets",
			fullscreen: false,
			filter: (file) => {
				return file.media || file.directory;
			},
			onSelect: async (file: any) => {
				if (file && file.uri) {
					var value = file.uri; // Use the file's URI
					if (file.uri.startsWith("/")) {
						value = file.uri.substring(1); // Remove leading slash if present
					}

					var imageUrl = value;
					if (value && value != '') {
						imageUrl = patchPathWithContext("/media/" + value)
					}
					let altText = file.title || file.name || "Image";

					// select media format
					var mediaFormats  = (await getMediaFormats({})).result || [];
					var formatOptions = {};
					formatOptions["original"] = "Original";
					mediaFormats.forEach((format : any) => {
						formatOptions[format.name] = format.name;
					});
					console.log("Media Formats", mediaFormats, formatOptions);
					var selectedFormat = await alertSelect({
						title: i18n.t("form.media.format.title", "Select Media Format"),
						values: formatOptions
					})
					if (selectedFormat && selectedFormat !== "original") {
						imageUrl += "?format=" + selectedFormat
					}

					const editor = getEditorFromEvent(event);

					const cm = editor.editor?.editor;
					if (cm) {
						const markdown = `![${altText}](${imageUrl})`;
						cm.replaceSelection(markdown, "end");
						cm.focus();
					}
				}
			}
		})
	}
});