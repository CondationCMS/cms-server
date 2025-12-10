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
import { openModal } from '@cms/modules/modal.js'
import { showToast } from '@cms/modules/toast.js'
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js'
import Sortable from 'https://cdn.jsdelivr.net/npm/sortablejs@1.15.6/+esm'
import Handlebars from 'https://cdn.jsdelivr.net/npm/handlebars@4.7.8/+esm'
import {getContentNode, setMetaBatch} from '@cms/modules/rpc/rpc-content.js'
// hook.js
export async function runAction(params) {

	const contentNode = await getContentNode({
		url: getPreviewUrl()
	})

	var template = Handlebars.compile(`
			<ul class="list-group cms-sortable">
				{{#each sections}}
    				<li class="list-group-item" data-cms-section-uri="{{uri}}" data-cms-section-index="{{index}}"><i class="bi bi-grip-vertical" data-cms-section-handle=''></i> 
					{{#if data.title}}
          				{{data.title}}
					{{else}}
						{{uri}}
					{{/if}}
					</li>
  				{{/each}}
			</ul>
		`);
	var sections = []
	if (contentNode.result.sections[params.sectionName]) {
		var sections = contentNode.result.sections[params.sectionName]
	}
	sections = sections.sort((a, b) => a.index - b.index)


	openModal({
		title: 'Edit Sections',
		body: template({ sections: sections }),
		fullscreen: false,
		onCancel: (event) => {},
		onOk: async (event) => {
			await saveSections();
			showToast({
				title: 'Sections saved',
				message: 'Sections successfuly saved.',
				type: 'success', // optional: info | success | warning | error
				timeout: 3000
			});
			await new Promise(resolve => setTimeout(resolve, 1000));
			reloadPreview()
		},
		onShow: () => {
			document.querySelectorAll(".cms-sortable").forEach(elem => {
				var sortable = Sortable.create(elem, {
					handle: '[data-cms-section-handle]'
				});
			})
		}
	});
}

const saveSections = async () => {

	const items = document.querySelectorAll(".cms-sortable li");

	var updates = []
	for (const [index, el] of Array.from(items).entries()) {
		var uri = el.dataset.cmsSectionUri

		updates.push({
			uri: uri,
			meta: {
				"layout.order" : {
					type: 'number',
					value: parseInt(index)
				}
			}
		});
	}
	await setMetaBatch({
		updates: updates
	});
}
