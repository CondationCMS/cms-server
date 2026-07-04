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
import Handlebars from 'https://cdn.jsdelivr.net/npm/handlebars@4.7.8/+esm';
import { patchPathWithContext } from "@cms/js/manager-globals";
Handlebars.registerHelper("patchPathWithContext", patchPathWithContext);
Handlebars.registerHelper('concat', function (...args) {
    args.pop();
    return args.join('');
});
Handlebars.registerHelper('ifNotEquals', function (arg1, arg2, options) {
    return (arg1 !== arg2) ? options.fn(this) : options.inverse(this);
});
const template = Handlebars.compile(`
	<div>
		<div class="cms-media-browser-slider" id="cms-media-browser-slider">
			<div class="cms-media-browser-panel cms-media-browser-panel--browse">
				<div class="d-flex gap-3 mb-3">
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
				</div>

				<div class="cms-media-grid" id="cms-filebrowser-files">
				{{#each files}}
					<div class="cms-media-card"
						data-cms-file-uri="{{uri}}"
						data-cms-file-name="{{name}}"
						{{#if directory}}data-cms-file-directory="true"{{/if}}>
						<div class="cms-media-card__preview">
							{{#if directory}}
								<i class="bi bi-folder cms-media-icon"></i>
							{{else if media}}
								<img src="{{patchPathWithContext (concat "/assets" uri)}}" alt="{{name}}" />
							{{else}}
								<i class="bi bi-file-earmark cms-media-icon"></i>
							{{/if}}
						</div>
						<div class="cms-media-card__footer">
							<span class="cms-media-card__name" title="{{name}}">{{name}}</span>
							{{#ifNotEquals name ".."}}
							<div class="dropdown">
								<button class="btn btn-sm p-0 px-1 dropdown-nocaret" type="button" data-bs-toggle="dropdown" aria-expanded="false">
									<i class="bi bi-three-dots-vertical"></i>
								</button>
								<ul class="dropdown-menu dropdown-menu-end">
									{{#if directory}}
										<li><a class="dropdown-item" href="#" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFolder">
											<i class="bi bi-folder-x me-2"></i>Delete folder
										</a></li>
										<li><a class="dropdown-item" href="#" data-cms-file-uri="{{uri}}" data-cms-file-action="renameFile">
											<i class="bi bi-pencil-square me-2"></i>Rename
										</a></li>
									{{else}}
										<li><a class="dropdown-item" href="#" data-cms-file-uri="{{uri}}" data-cms-file-action="editMetadata">
											<i class="bi bi-card-text me-2"></i>Edit metadata
										</a></li>
										<li><a class="dropdown-item" href="#" data-cms-file-uri="{{uri}}" data-cms-file-action="deleteFile">
											<i class="bi bi-file-earmark-x me-2"></i>Delete file
										</a></li>
										<li><a class="dropdown-item" href="#" data-cms-file-uri="{{uri}}" data-cms-file-action="renameFile">
											<i class="bi bi-pencil-square me-2"></i>Rename
										</a></li>
									{{/if}}
								</ul>
							</div>
							{{/ifNotEquals}}
						</div>
					</div>
				{{/each}}
				</div>

				{{#if asset}}
					<div class="mt-3">
						<input id="cms-fileupload" type="file" name="cms-fileupload" accept="image/png, image/jpeg, image/web, image/gif, image/svg+xml, image/tiff, image/avif" />
						<button id="cms-filebrowser-upload-button" class="btn btn-secondary btn-sm">Upload</button>
						<span id="cms-filebrowser-upload-progress"></span>
						<div id="drop-zone">Drop files here</div>
					</div>
				{{/if}}
			</div>

			<div class="cms-media-browser-panel cms-media-browser-panel--metadata">
				<div class="d-flex align-items-center justify-content-between mb-3">
					<h6 class="mb-0" id="cms-media-metadata-title">Media attributes</h6>
					<button type="button" class="btn btn-sm btn-secondary" id="cms-media-metadata-back">Back</button>
				</div>
				<div id="cms-media-metadata-form"></div>
			</div>
		</div>
	</div>
`);
export { template as mediabrowserTemplate };
