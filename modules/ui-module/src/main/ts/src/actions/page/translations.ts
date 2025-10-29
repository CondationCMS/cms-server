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
import { openFileBrowser } from '../../js/modules/filebrowser.js'
import { i18n } from '../../js/modules/localization.js'
import { openModal } from '../../js/modules/modal.js'
import { getPreviewUrl } from '../../js/modules/preview.utils.js'
import { getContentNode } from '../../js/modules/rpc/rpc-content.js'
import { addTranslation, getTranslations, TranslationDto } from '../../js/modules/rpc/rpc-translation.js'
import { showToast } from '../../js/modules/toast.js'
// hook.js
export async function runAction(params: any) {

	const contentNode = await getContentNode({
		url: getPreviewUrl()
	})
	const uri = contentNode.result.uri

	var translations = await getTranslations({ uri: uri })
	console.log('Translations:', translations);

	var modelContent = createTranslationsTable(translations.translations);

	openModal({
		title: 'Manage Translations',
		body: modelContent,
		onCancel: (event) => { },
		onOk: async (event) => {
		},
		onShow: async (modalElement) => {

			modalElement.querySelectorAll('button[data-action]').forEach(button => {
				button.addEventListener('click', async (e) => {
					const action = (e.currentTarget as HTMLElement).getAttribute('data-action');
					const siteId = (e.currentTarget as HTMLElement).getAttribute('data-id');
					const lang = (e.currentTarget as HTMLElement).getAttribute('data-lang');
					if (action === 'select') {
						// Open file browser to select existing translation
						openFileBrowser({
							siteId: siteId || '',
							type: 'content',
							onSelect: async (file) => {
								console.log('Selected translation file:', file);
								if (file && file.uri) {
									var selectedFile = file.uri; // Use the file's URI
									
									await addTranslation({
										uri: uri,
										language: lang || '',
										translationUri: selectedFile
									});
									showToast({
										title: i18n.t('manager.translation.added.title', "Translation Added"),
										message: i18n.t('manager.translation.added.message', "Translation successfuly added."),
										type: 'success', // optional: info | success | warning | error
										timeout: 3000
									});
								}
							}
						})
					}
				})
			})
		}
	})
}

function createTranslationsTable(translations: TranslationDto[]): string {
	const table = document.createElement('table');
	table.className = 'table table-striped table-bordered';
	table.innerHTML = `
        <thead>
            <tr>
                <th>Language</th>
				<th>Site</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            ${translations.map(translation => createTranslationRow(translation)).join('')}
        </tbody>
    `;
	return table.outerHTML;
}

function createTranslationRow(translation: TranslationDto): string {
	const status = translation.managerDeepLink ? `<a href="${translation.managerDeepLink}" target="_blank">Linked</a>` : 'Not linked';
	return `
        <tr>
            <td><span class="fi fi-${translation.country}"></span></td>
            <td>${translation.site}</td>
            <td>${status}</td>
            <td>
                ${getActionButtons(translation)}
            </td>
        </tr>
    `;
}

function getActionButtons(translation: TranslationDto): string {
	let buttons = '';
	buttons += `<button class="btn btn-sm btn-primary" data-action="select" data-lang="${translation.lang}" data-id="${translation.site}">Select</button>`;
	if (translation.url) {
		buttons += `<button class="btn btn-sm btn-danger" data-action="remove" data-lang="${translation.lang}" data-id="${translation.site}">Remove</button>`;
	}
	return buttons;
}
