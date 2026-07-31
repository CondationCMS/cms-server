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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import { i18n } from '@cms/modules/localization.js';
import { openModal } from '@cms/modules/modal.js';
import { searchPages, SearchResultDto } from '@cms/modules/rpc/rpc-page.js';

const MIN_QUERY_LENGTH = 3;

export interface PagePickerOptions {
	title?: string;
	selectText?: string;
	onSelect: (page: SearchResultDto) => void | Promise<void>;
}

const escapeHtml = (value: string): string => value
	.replace(/&/g, '&amp;')
	.replace(/</g, '&lt;')
	.replace(/>/g, '&gt;')
	.replace(/"/g, '&quot;')
	.replace(/'/g, '&#039;');

const renderMessage = (message: string): string =>
	`<p class="text-body-secondary mb-0">${escapeHtml(message)}</p>`;

const renderResults = (results: SearchResultDto[], selectText: string): string => {
	if (results.length === 0) {
		return renderMessage(i18n.t('page.search.noResults', 'No pages found.'));
	}

	return `
		<div class="list-group">
			${results.map((result, index) => `
				<button type="button" class="list-group-item list-group-item-action d-flex gap-3 align-items-center"
					data-page-picker-index="${index}">
					<span class="flex-grow-1 text-start overflow-hidden">
						<strong class="d-block text-truncate">${escapeHtml(result.title || result.url)}</strong>
						<code class="d-block text-truncate">${escapeHtml(result.url)}</code>
					</span>
					<span class="btn btn-outline-primary btn-sm">${escapeHtml(selectText)}</span>
				</button>`).join('')}
		</div>`;
};

export const openPagePicker = (options: PagePickerOptions): void => {
	let modal: any;
	let searchVersion = 0;
	let debounceTimer: number | undefined;
	const selectText = options.selectText || i18n.t('page.picker.select', 'Select');

	modal = openModal({
		title: options.title || i18n.t('page.picker.title', 'Select page'),
		body: `
			<label class="form-label" for="cms-page-picker-input">
				${i18n.t('page.picker.searchLabel', 'Search by page title')}
			</label>
			<input type="search" class="form-control" id="cms-page-picker-input"
				placeholder="${i18n.t('page.search.placeholder', 'Search by title...')}" autocomplete="off">
			<div class="mt-3" data-page-picker-results></div>`,
		fullscreen: false,
		size: 'lg',
		showFooter: false,
		onShow: (modalElement: HTMLElement) => {
			const input = modalElement.querySelector('#cms-page-picker-input') as HTMLInputElement;
			const resultsElement = modalElement.querySelector('[data-page-picker-results]') as HTMLElement;

			const showHint = () => {
				resultsElement.innerHTML = renderMessage(i18n.t(
					'page.search.minLength',
					'Enter at least 3 characters to search.'
				));
			};

			const runSearch = async () => {
				const query = input.value.trim();
				if (query.length < MIN_QUERY_LENGTH) {
					searchVersion++;
					showHint();
					return;
				}

				const version = ++searchVersion;
				resultsElement.innerHTML = `
					<div class="d-flex align-items-center gap-2 text-body-secondary">
						<span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
						${i18n.t('page.picker.searching', 'Searching pages...')}
					</div>`;
				try {
					const response = await searchPages({ query });
					if (version !== searchVersion) return;
					const results = response.result || [];
					resultsElement.innerHTML = renderResults(results, selectText);
					resultsElement.querySelectorAll<HTMLElement>('[data-page-picker-index]').forEach(button => {
						button.addEventListener('click', async () => {
							const result = results[Number(button.dataset.pagePickerIndex)];
							if (!result) return;
							await options.onSelect(result);
							modal.hide();
						});
					});
				} catch (error) {
					if (version !== searchVersion) return;
					resultsElement.innerHTML = `
						<div class="alert alert-danger mb-0">
							${i18n.t('page.search.loadError', 'Could not search pages.')}
						</div>`;
				}
			};

			input.addEventListener('input', () => {
				window.clearTimeout(debounceTimer);
				debounceTimer = window.setTimeout(runSearch, 300);
			});
			input.addEventListener('keydown', event => {
				if (event.key === 'Enter') {
					event.preventDefault();
					window.clearTimeout(debounceTimer);
					runSearch();
				}
			});

			showHint();
			input.focus();
		}
	});
};
