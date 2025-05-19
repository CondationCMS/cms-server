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

const defaultOptions = {
	validate: () => true
};

const template = Handlebars.compile(`
	<table class="table table-hover">
		<thead>
			<tr>
				<th scope="col"></th>
				<th scope="col">Filename</th>
				<th scope="col">Actions</th>
			</tr>
		</thead>
		<tbody>
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
`);

const openFileBrowser = async (optionsParam) => {
	const options = {
		...defaultOptions,
		...optionsParam
	};

	options.modal = openModal({
		title: 'Filesystem',
		body: '<div id="cms-file-browser"></div>',
		fullscreen: true,
		onOk: async (event) => {
			console.log("modal ok");
		},
		onShow: async () => {
			initFileBrowser(options);
		}
	});

};

const initFileBrowser = async (options, uri) => {
	const contentFiles = await listFiles({
		type: options.type,
		uri: uri ? uri : null
	})

	const fileBrowserElement = document.getElementById("cms-file-browser");
	if (fileBrowserElement) {
		fileBrowserElement.innerHTML = template({ files: contentFiles.result.files });
		makeDirectoriesClickable(options)
		fileActions(options);
	}
};

const makeDirectoriesClickable = (options) => {
	const elements = document.querySelectorAll("[data-cms-file-directory]");
	elements.forEach((element) => {
		element.addEventListener("dblclick", (event) => {
			event.stopPropagation();
			const directory = element.getAttribute("data-cms-file-uri");
			if (directory) {
				initFileBrowser(options, directory);
			}
		});
	});
};

const fileActions = (options) => {
	const elements = document.querySelectorAll("[data-cms-file-action]");
	elements.forEach((element) => {
		element.addEventListener("click", (event) => {
			event.stopPropagation();
			const uri = element.getAttribute("data-cms-file-uri");
			const action = element.getAttribute("data-cms-file-action");

			if (action === "open") {
				loadPreview(uri);
				options.modal.hide();
			}
		});
	});

};

export { openFileBrowser };
