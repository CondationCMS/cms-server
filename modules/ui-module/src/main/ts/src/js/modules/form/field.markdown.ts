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
import { createID } from "./utils.js";
import { i18n } from "../localization.js"
import { getTagNames } from "../rpc/rpc-manager.js";

let cherryEditors = [];

export interface MarkdownFieldOptions {
	name: string;
	placeholder?: string;
	title?: string;
	height?: string;
}

const createMarkdownField = (options: MarkdownFieldOptions, value: string = '') => {
	const id = createID();
	const key = "field." + options.name
	const title = i18n.t(key, options.title)
	return `
		<div class="mb-3 h-100 cms-form-field" data-cms-form-field-type="markdown" >
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			<div id="${id}" class="cherry-editor-container" style="height: ${options.height || '300px'}; border: 1px solid #ccc;"></div>
			<input type="hidden" name="${options.name}" data-cherry-id="${id}" data-initial-value="${encodeURIComponent(value)}">
		</div>
	`;
};

const getData = () => {
	const data = {};
	cherryEditors.forEach(({ input, editor }) => {
		data[input.name] = {
			type: "markdown",
			value: editor.getMarkdown()
		}
	});
	return data;
};

const init = async () => {
	cherryEditors = [];

	const cmsTagsMenu = await buildCmsTagsMenu();

	const editorInputs = document.querySelectorAll('[data-cms-form-field-type="markdown"] input');
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
					'cmsTagsMenu'
				],
				bubble: ['bold', 'italic', 'underline', 'strikethrough', 'sub', 'sup', 'quote', '|', 'size', 'color'], // array or false
				float: ['h1', 'h2', 'h3', '|', 'checklist', 'table', 'code'],
				customMenu: {
					cmsTagsMenu: cmsTagsMenu,
				},
			}
		});

		cherryEditors.push({ input, editor });
	});
};

export const MarkdownField = {
	markup: createMarkdownField,
	init: init,
	data: getData
};

const buildCmsTagsMenu = async () => {
	const response = await getTagNames({});
	const tagNames = response.result || [];

	const submenuConfig = tagNames.map(tag => ({
		name: tag.charAt(0).toUpperCase() + tag.slice(1),
		value: tag,
		noIcon: true,
		onclick: (event) => {
			const editorId = event.target.closest('.cherry-editor-container')?.id;
			const editor = cherryEditors.find(e => e.input.dataset.cherryId === editorId)?.editor;
			if (editor) {
				editor.toolbar.menus.hooks["cmsTagsMenu"].fire(null, tag);
			}
		}
	}));

	return window.Cherry.createMenuHook("cmsTagsMenu", {
		title: "CMS Tags",
		text: "CMS Tags",
		onClick: (selection, tag) => {
			return `[[${tag}]]${selection || ""}[[/${tag}]]`;
		},
		subMenuConfig: submenuConfig
	});
};

const cmsTagsMenuleg = window.Cherry.createMenuHook("CMS-Tags", {
	title: "CMS Tags",
	text: "CMS Tags",
	onClick: (selection, tag) => {
		return `[[${tag}]]${selection || ""}[[/${tag}]]`
	},
	subMenuConfig: [
		{
			name: "Info",
			noIcon: true,
			onclick: (event) => {
				const editorId = event.target.closest('.cherry-editor-container')?.id;
				const editor = cherryEditors.find(e => e.input.dataset.cherryId === editorId)?.editor;
				editor.toolbar.menus.hooks.cmsTagsMenu.fire(null, 'info');
			}
		},
		{
			name: "Warning",
			value: "warning",
			onclick: (event) => {
				const editorId = event.target.closest('.cherry-editor-container')?.id;
				const editor = cherryEditors.find(e => e.input.dataset.cherryId === editorId)?.editor;
				editor.toolbar.menus.hooks.cmsTagsMenu.fire(null, 'warning');
			}
		},
		// Weitere Tags ...
	]
});