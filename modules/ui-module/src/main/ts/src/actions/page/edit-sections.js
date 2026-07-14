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
				{{#each sectionEntries}}
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
	var sectionEntries = []
	if (contentNode.result.sections[params.section]) {
		var sectionEntries = contentNode.result.sections[params.section]
	}
	sectionEntries = sectionEntries.sort((a, b) => a.index - b.index)


	openModal({
		title: 'Edit items',
		body: template({ sectionEntries: sectionEntries }),
		fullscreen: false,
		onCancel: (event) => {},
		onOk: async (event) => {
			try {
				await saveSections();
				showToast({
					title: 'Entry saved',
					message: 'Entry successfuly saved.',
					type: 'success', // optional: info | success | warning | error
					timeout: 3000
				});
				await new Promise(resolve => setTimeout(resolve, 1000));
				reloadPreview()
			} catch (e) {
				showToast({
					title: 'Entry not saved',
					message: e.message,
					type: 'error',
					timeout: 3000
				});
			}
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
