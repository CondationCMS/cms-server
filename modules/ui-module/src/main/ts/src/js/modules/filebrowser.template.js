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

import Handlebars  from 'https://cdn.jsdelivr.net/npm/handlebars@4.7.8/+esm'
import { patchPathWithContext } from "@cms/js/manager-globals";

Handlebars.registerHelper("patchPathWithContext", patchPathWithContext);
Handlebars.registerHelper('concat', function (...args) {
  args.pop();
  return args.join('');
});
Handlebars.registerHelper('ifNotEquals', function(arg1, arg2, options) {
    return (arg1 !== arg2) ? options.fn(this) : options.inverse(this);
});

Handlebars.registerPartial('fileBrowserContentActions', `
	<div class="dropdown">
		<button class="btn btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
			Create Content
		</button>
		<ul class="dropdown-menu">
			{{#each pageTemplates}}
				<li><a class="dropdown-item" data-cms-filbrowser-ct-action="create" href="#" data-cms-contenttype="{{name}}">{{name}}</a></li>
			{{/each}}
		</ul>
	</div>
`);

const template = Handlebars.compile(`
	<div>
		<div class="d-flex gap-3">
			<div class="dropdown">
				<button class="btn btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
					Actions
				</button>
				<ul class="dropdown-menu">
					{{#each actions}}
						<li><a class="dropdown-item" href="#" id="{{id}}">{{name}}</a></li>
					{{/each}}
				</ul>
			</div>

			{{#unless asset}}
				{{> fileBrowserContentActions pageTemplates=pageContentTypes }} 
			{{/unless}}
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
				data-cms-file-name="{{name}}"
				{{#if directory}} data-cms-file-directory="true"{{/if}}>
				<th scope="row">
					{{#if directory}}
						<i class="bi bi-folder"></i>
					{{else if media}}
						<div class="position-relative d-inline-block cms-image-hover-wrapper">
							<img src="{{patchPathWithContext (concat "/assets" uri)}}" alt="{{name}}" class="img-thumbnail cms-small-image" />
							<div class="cms-overlay-image">
								<img src="{{patchPathWithContext (concat "/assets" uri)}}" alt="Zoom" class="cms-enlarged-image" />
							</div>
						</div>
					{{else}}
						<i class="bi bi-file"></i>
					{{/if}}
				</th>
				<td>{{name}}</td>
				<td>
					{{#if directory}}
						{{#ifNotEquals name ".."}}
							<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFolder"
								data-bs-toggle="tooltip" data-bs-placement="top"
								data-bs-title="Delete folder."
							>
								<i class="bi bi-folder-x"></i>
							</button>
						{{/ifNotEquals}}
					{{else if content}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="open"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Open page."
						>
							<i class="bi bi-file-arrow-up"></i>
							</button>
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deletePage"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Delete page."
						>
							<i class="bi bi-file-earmark-x"></i>
						</button>
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="copyUrl"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Copy url."
						>
							<i class="bi bi-copy"></i>
						</button>
					{{else}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFile"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Delete file."
						>
							<i class="bi bi-file-earmark-x"></i>
						</button>
					{{/if}}
					{{#ifNotEquals name ".."}}
						<button class="btn" data-cms-file-uri="{{uri}}" data-cms-file-action="renameFile"
							data-bs-toggle="tooltip" data-bs-placement="top"
        					data-bs-title="Rename file."
						>
							<i class="bi bi-pencil-square"></i>
						</button>
					{{/ifNotEquals}}
				</td>
			<tr>
		{{/each}}
		</tbody>
	</table>
	{{#if asset}} 
		<input id="cms-fileupload" type="file" name="cms-fileupload" accept="image/png, image/jpeg, image/web, image/gif, image/svg+xml, image/tiff, image/avif" />
		<button id="cms-filebrowser-upload-button"> Upload </button>
		<span id="cms-filebrowser-upload-progress"></span>
		<div id="drop-zone">Drop files here</div>
	{{/if}}
	</div>
`);

export {template as filebrowserTemplate};