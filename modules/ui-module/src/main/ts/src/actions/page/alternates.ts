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

import { openFileBrowser } from '@cms/modules/filebrowser/filebrowser.js'
import { openModal } from '@cms/modules/modal.js'
import { getPreviewUrl } from '@cms/modules/preview.utils.js'
import { getContentNode } from '@cms/modules/rpc/rpc-content.js'
import { addAlternate, getAlternates, removeAlternate, AlternateDto } from '@cms/modules/rpc/rpc-alternates.js'
import { showToast } from '@cms/modules/toast.js'
import { ensureAlternatesSupported } from '@cms/modules/alternate-support.js'

export async function runAction(params: any) {
	const contentNode = await getContentNode({ url: getPreviewUrl() })
	if (!ensureAlternatesSupported(contentNode.result)) {
		return
	}
	const uri = contentNode.result.uri
	const response = await getAlternates({ uri })

	openModal({
		title: 'Manage Alternates',
		body: createAlternatesTable(response.alternates),
		onCancel: () => {},
		onOk: async () => {},
		onShow: async (modalElement: HTMLElement) => {
			modalElement.querySelectorAll<HTMLElement>('button[data-action]').forEach(button => {
				button.addEventListener('click', async event => {
					const element = event.currentTarget as HTMLElement
					const action = element.dataset.action
					const targetSite = element.dataset.site || ''

					if (action === 'select') {
						openFileBrowser({
							siteId: targetSite,
							type: 'content',
							onSelect: async (file: any) => {
								if (!file?.url) return
								await addAlternate({ uri, targetSite, alternateUri: file.url })
								showToast({ title: 'Alternate added', message: 'The alternate page was linked.', type: 'success', timeout: 3000 })
							}
						})
					} else if (action === 'remove') {
						await removeAlternate({ uri, targetSite })
						showToast({ title: 'Alternate removed', message: 'The alternate page link was removed.', type: 'success', timeout: 3000 })
					}
				})
			})
		}
	})
}

function createAlternatesTable(alternates: AlternateDto[]): string {
	return `<table class="table table-striped table-bordered">
		<thead><tr><th>Site</th><th>Locale</th><th>Status</th><th>Actions</th></tr></thead>
		<tbody>${alternates.map(createAlternateRow).join('')}</tbody>
	</table>`
}

function createAlternateRow(alternate: AlternateDto): string {
	const status = alternate.managerDeepLink ? `<a href="${alternate.managerDeepLink}" target="_blank">Linked</a>` : 'Not linked'
	return `<tr><td>${alternate.site}</td><td>${alternate.locale}</td><td>${status}</td><td>${actionButtons(alternate)}</td></tr>`
}

function actionButtons(alternate: AlternateDto): string {
	let buttons = `<button class="btn btn-sm btn-primary" data-action="select" data-site="${alternate.site}">Select</button>`
	if (alternate.url) {
		buttons += ` <button class="btn btn-sm btn-danger" data-action="remove" data-site="${alternate.site}">Remove</button>`
	}
	return buttons
}
