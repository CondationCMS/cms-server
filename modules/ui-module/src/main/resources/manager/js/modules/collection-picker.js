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
import { listCollectionItems } from '@cms/modules/rpc/rpc-collection.js';
const PAGE_SIZE = 10;
const escapeHtml = (value) => String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
const renderMessage = (message) => `<p class="text-body-secondary mb-0">${escapeHtml(message)}</p>`;
const renderResults = (response, selectText) => {
    if (response.items.length === 0) {
        return renderMessage(i18n.t('collection.picker.noResults', 'No collection items found.'));
    }
    const page = Math.max(1, response.page);
    const totalPages = Math.max(1, response.totalPages);
    return `
		<div class="list-group">
			${response.items.map((item, index) => `
				<button type="button" class="list-group-item list-group-item-action d-flex gap-3 align-items-center"
					data-collection-picker-index="${index}">
					<span class="flex-grow-1 text-start overflow-hidden">
						<strong class="d-block text-truncate">${escapeHtml(item.title || item.id)}</strong>
						<code class="d-block text-truncate">${escapeHtml(item.id)}</code>
					</span>
					<span class="btn btn-outline-primary btn-sm">${escapeHtml(selectText)}</span>
				</button>`).join('')}
		</div>
		<div class="d-flex justify-content-between align-items-center gap-2 mt-3">
			<button type="button" class="btn btn-outline-secondary btn-sm"
				data-collection-picker-page="${page - 1}"
				${page <= 1 ? 'disabled' : ''}>
				${escapeHtml(i18n.t('collection.picker.previous', 'Previous'))}
			</button>
			<span class="text-body-secondary small">
				${escapeHtml(i18n.t('collection.picker.page', 'Page'))} ${page} / ${totalPages}
			</span>
			<button type="button" class="btn btn-outline-secondary btn-sm"
				data-collection-picker-page="${page + 1}"
				${page >= totalPages ? 'disabled' : ''}>
				${escapeHtml(i18n.t('collection.picker.next', 'Next'))}
			</button>
		</div>`;
};
export const openCollectionItemPicker = (options) => {
    let modal;
    let requestVersion = 0;
    let debounceTimer;
    let currentPage = 1;
    const inputId = `cms-collection-picker-${Date.now()}`;
    const selectText = options.selectText || i18n.t('collection.picker.select', 'Select');
    modal = openModal({
        title: options.title || i18n.t('collection.picker.title', 'Select collection item'),
        body: `
			<label class="form-label" for="${inputId}">
				${escapeHtml(i18n.t('collection.picker.searchLabel', 'Search by title'))}
			</label>
			<input type="search" class="form-control" id="${inputId}"
				placeholder="${escapeHtml(i18n.t('collection.picker.searchPlaceholder', 'Search by title...'))}"
				autocomplete="off">
			<div class="mt-3" data-collection-picker-results></div>`,
        fullscreen: false,
        size: 'lg',
        showFooter: false,
        onShow: (modalElement) => {
            const input = modalElement.querySelector(`#${inputId}`);
            const resultsElement = modalElement.querySelector('[data-collection-picker-results]');
            const loadPage = async () => {
                const version = ++requestVersion;
                resultsElement.innerHTML = `
					<div class="d-flex align-items-center gap-2 text-body-secondary">
						<span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
						${escapeHtml(i18n.t('collection.picker.loading', 'Loading collection items...'))}
					</div>`;
                try {
                    const response = await listCollectionItems({
                        collection: options.collection,
                        query: input.value.trim() || undefined,
                        page: currentPage,
                        size: PAGE_SIZE
                    });
                    if (version !== requestVersion)
                        return;
                    currentPage = response.page;
                    resultsElement.innerHTML = renderResults(response, selectText);
                    resultsElement.querySelectorAll('[data-collection-picker-index]').forEach(button => {
                        button.addEventListener('click', async () => {
                            const item = response.items[Number(button.dataset.collectionPickerIndex)];
                            if (!item)
                                return;
                            await options.onSelect(item);
                            modal.hide();
                        });
                    });
                    resultsElement.querySelectorAll('[data-collection-picker-page]')
                        .forEach(button => button.addEventListener('click', () => {
                        const targetPage = Number(button.dataset.collectionPickerPage);
                        if (button.disabled || !Number.isInteger(targetPage) || targetPage < 1
                            || targetPage > response.totalPages || targetPage === currentPage) {
                            return;
                        }
                        currentPage = targetPage;
                        loadPage();
                    }));
                }
                catch (error) {
                    // exception is ignored, message to user is displayed in the modal
                    if (version !== requestVersion)
                        return;
                    resultsElement.innerHTML = `
						<div class="alert alert-danger mb-0">
							${escapeHtml(i18n.t('collection.picker.loadError', 'Could not load collection items.'))}
						</div>`;
                }
            };
            input.addEventListener('input', () => {
                window.clearTimeout(debounceTimer);
                debounceTimer = window.setTimeout(() => {
                    currentPage = 1;
                    loadPage();
                }, 300);
            });
            input.addEventListener('keydown', event => {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    window.clearTimeout(debounceTimer);
                    currentPage = 1;
                    loadPage();
                }
            });
            loadPage();
            input.focus();
        }
    });
};
