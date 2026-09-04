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

import { openCollectionItemCreator } from './create-collection-item.js';
import { CollectionItemEditor, createCollectionItemEditor } from './edit-collection-item.js';
import { i18n } from '@cms/modules/localization.js';
import { openModal } from '@cms/modules/modal.js';
import { loadPreview } from '@cms/modules/preview.utils.js';
import { CollectionItemSummary, deleteCollectionItem, listCollectionItemsCursor } from '@cms/modules/rpc/rpc-collection.js';
import { showToast } from '@cms/modules/toast.js';

const PAGE_SIZE = 10;
const MIN_SEARCH_LENGTH = 3;

const escapeHtml = (value: string | number | boolean | null | undefined): string =>
    String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');

const renderItems = (items: CollectionItemSummary[]): string => {
	if (items.length === 0) {
		return `<p class="text-muted mb-0">${i18n.t('collection.items.empty', 'No collection items found.')}</p>`;
	}
	return `<div class="list-group">${items.map(item => `
		<div class="list-group-item d-flex justify-content-between align-items-center gap-3">
			<div class="text-truncate">
				<strong>${escapeHtml(item.title)}</strong><br>
				<small class="text-muted">${escapeHtml(item.id)}</small>
			</div>
			<div class="btn-group btn-group-sm flex-shrink-0">
				<button type="button" class="btn btn-outline-primary" data-collection-edit="${escapeHtml(item.id)}">
					${i18n.t('collection.items.edit', 'Edit')}
				</button>
				${item.detailUrl ? `<button type="button" class="btn btn-outline-secondary"
					data-collection-open="${escapeHtml(item.detailUrl)}">
					${i18n.t('collection.items.open', 'Open detail page')}
				</button>` : ''}
				<button type="button" class="btn btn-outline-danger" data-collection-delete="${escapeHtml(item.id)}"
					title="${i18n.t('collection.items.delete', 'Delete')}">
					<i class="bi bi-trash"></i>
				</button>
			</div>
		</div>`).join('')}</div>`;
};

export const runAction = async (options: { collection: string }) => {
	let currentPage = 1;
	let cursorHistory = [''];
	let currentQuery = '';
	let requestVersion = 0;
	let editorRequestVersion = 0;
	let modal: any;
	let modalElement: HTMLElement | null = null;
	let collectionSlider: HTMLElement | null = null;
	let browsePanel: HTMLElement | null = null;
	let editorPanel: HTMLElement | null = null;
	let editorContent: HTMLElement | null = null;
	let editorSaveButton: HTMLButtonElement | null = null;
	let currentEditor: CollectionItemEditor | null = null;
	let isSaving = false;

	const body = `
		<div class="cms-collection-manager-slider" data-collection-slider>
			<section class="cms-collection-manager-panel cms-collection-manager-panel--browse" data-collection-browse-panel>
				<div class="d-flex justify-content-between align-items-end gap-3">
					<div class="flex-grow-1">
						<label class="form-label" for="cms-collection-search">
							${i18n.t('collection.items.searchLabel', 'Search by title')}
						</label>
						<input type="search" class="form-control" id="cms-collection-search"
							placeholder="${i18n.t('collection.items.searchPlaceholder', 'Search by title...')}" autocomplete="off">
					</div>
					<button type="button" class="btn btn-primary flex-shrink-0" data-collection-create>
						<i class="bi bi-plus-lg"></i> ${i18n.t('collection.items.create', 'New item')}
					</button>
				</div>
				<div class="mt-3" data-collection-results></div>
				<div class="mt-3" data-collection-pagination></div>
			</section>
			<section class="cms-collection-manager-panel cms-collection-manager-panel--editor"
				data-collection-editor-panel aria-hidden="true" inert>
				<div class="d-flex align-items-center justify-content-between gap-3 mb-3">
					<h6 class="mb-0">${i18n.t('collection.item.edit.title', 'Edit collection item')}</h6>
					<button type="button" class="btn btn-sm btn-secondary" data-collection-editor-back>
						${i18n.t('buttons.back', 'Back')}
					</button>
				</div>
				<div data-collection-editor-content></div>
				<div class="d-flex justify-content-end gap-2 mt-3">
					<button type="button" class="btn btn-secondary" data-collection-editor-cancel>
						${i18n.t('buttons.cancel', 'Cancel')}
					</button>
					<button type="button" class="btn btn-primary" data-collection-editor-save disabled>
						${i18n.t('buttons.save', 'Save')}
					</button>
				</div>
			</section>
		</div>`;

	const closeEditor = () => {
		editorRequestVersion++;
		currentEditor = null;
		isSaving = false;
		if (editorSaveButton) editorSaveButton.disabled = true;
		collectionSlider?.classList.remove('is-editing-item');
		browsePanel?.setAttribute('aria-hidden', 'false');
		editorPanel?.setAttribute('aria-hidden', 'true');
		if (browsePanel) browsePanel.inert = false;
		if (editorPanel) editorPanel.inert = true;
	};

	const openEditor = async (id: string) => {
		if (!collectionSlider || !editorContent || !editorSaveButton) return;
		const version = ++editorRequestVersion;
		currentEditor = null;
		editorSaveButton.disabled = true;
		editorContent.innerHTML = `<div class="text-muted">${i18n.t('collection.item.edit.loading', 'Loading collection item...')}</div>`;
		collectionSlider.classList.add('is-editing-item');
		browsePanel?.setAttribute('aria-hidden', 'true');
		editorPanel?.setAttribute('aria-hidden', 'false');
		if (browsePanel) browsePanel.inert = true;
		if (editorPanel) editorPanel.inert = false;

		try {
			const editor = await createCollectionItemEditor({
				collection: options.collection,
				id,
				onSaved: async () => {
					currentPage = 1;
					cursorHistory = [''];
					await update();
					closeEditor();
				}
			});
			if (version !== editorRequestVersion) return;
			currentEditor = editor;
			editorContent.innerHTML = '';
			editor.form.init(editorContent);
			editorSaveButton.disabled = false;
		} catch (error: any) {
			if (version !== editorRequestVersion) return;
			showToast({
				title: i18n.t('collection.item.edit.loadError.title', 'Collection item could not be loaded'),
				message: error?.message ?? String(error),
				type: 'error',
				timeout: 3000
			});
			closeEditor();
		}
	};

	const saveEditor = async () => {
		if (!currentEditor || !editorSaveButton || isSaving || !currentEditor.form.validate()) return;
		isSaving = true;
		editorSaveButton.disabled = true;
		const saved = await currentEditor.save();
		isSaving = false;
		if (!saved && currentEditor) editorSaveButton.disabled = false;
	};

	const update = async () => {
		const version = ++requestVersion;
		const root = modalElement?.querySelector('[data-collection-results]') as HTMLElement | null;
		const pagination = modalElement?.querySelector('[data-collection-pagination]') as HTMLElement | null;
		if (!root || !pagination) return;
		root.innerHTML = `<div class="text-muted">${i18n.t('collection.items.loading', 'Loading collection items...')}</div>`;
		try {
			const page = await listCollectionItemsCursor({
				collection: options.collection,
				query: currentQuery,
				cursor: cursorHistory[currentPage - 1],
				size: PAGE_SIZE
			});
			if (version !== requestVersion) return;
			if (page.items.length === 0 && currentPage > 1) {
				currentPage--;
				await update();
				return;
			}
			root.innerHTML = renderItems(page.items);
			pagination.innerHTML = currentPage > 1 || page.nextCursor ? `
				<nav aria-label="Collection pagination">
					<ul class="pagination justify-content-center mb-0">
						<li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
							<button type="button" class="page-link" data-collection-direction="previous">
								${i18n.t('pagination.previous', 'Previous')}
							</button>
						</li>
						<li class="page-item disabled"><span class="page-link">${currentPage}</span></li>
						<li class="page-item ${page.nextCursor ? '' : 'disabled'}">
							<button type="button" class="page-link" data-collection-direction="next">
								${i18n.t('pagination.next', 'Next')}
							</button>
						</li>
					</ul>
				</nav>` : '';

			root.querySelectorAll<HTMLElement>('[data-collection-edit]').forEach(button => {
				button.addEventListener('click', () => openEditor(button.dataset.collectionEdit ?? ''));
			});
			root.querySelectorAll<HTMLElement>('[data-collection-open]').forEach(button => {
				button.addEventListener('click', () => {
					modal.hide();
					loadPreview(button.dataset.collectionOpen ?? '');
				});
			});
			root.querySelectorAll<HTMLElement>('[data-collection-delete]').forEach(button => {
				button.addEventListener('click', async () => {
					const id = button.dataset.collectionDelete ?? '';
					const prompt = `${i18n.t('collection.items.deleteConfirm', 'Delete collection item')} “${id}”?`;
					if (!window.confirm(prompt)) return;
					try {
						await deleteCollectionItem(options.collection, id);
						currentPage = 1;
						cursorHistory = [''];
						showToast({
							title: i18n.t('collection.items.deleteSuccess.title', 'Collection item deleted'),
							message: i18n.t('collection.items.deleteSuccess.message', 'The collection item was deleted successfully.'),
							type: 'success',
							timeout: 3000
						});
						await update();
					} catch (error: any) {
						showToast({
							title: i18n.t('collection.items.deleteError.title', 'Collection item not deleted'),
							message: error?.message ?? String(error),
							type: 'error',
							timeout: 3000
						});
					}
				});
			});
			pagination.querySelectorAll<HTMLElement>('[data-collection-direction]').forEach(button => {
				button.addEventListener('click', () => {
					if (button.dataset.collectionDirection === 'next' && page.nextCursor) {
						cursorHistory[currentPage] = page.nextCursor;
						currentPage++;
					} else if (button.dataset.collectionDirection === 'previous' && currentPage > 1) {
						currentPage--;
					}
					update();
				});
			});
		} catch (error: any) {
			if (version !== requestVersion) return;
			root.innerHTML = `<div class="alert alert-danger">${escapeHtml(error?.message ?? error)}</div>`;
			pagination.innerHTML = '';
		}
	};

	modal = openModal({
		title: options.collection,
		body,
		size: 'xl',
		showFooter: false,
		onShow: (element: HTMLElement) => {
			modalElement = element;
			collectionSlider = element.querySelector('[data-collection-slider]');
			browsePanel = element.querySelector('[data-collection-browse-panel]');
			editorPanel = element.querySelector('[data-collection-editor-panel]');
			editorContent = element.querySelector('[data-collection-editor-content]');
			editorSaveButton = element.querySelector('[data-collection-editor-save]');
			const input = element.querySelector<HTMLInputElement>('#cms-collection-search');
			element.querySelectorAll('[data-collection-editor-back], [data-collection-editor-cancel]')
				.forEach(button => button.addEventListener('click', closeEditor));
			editorSaveButton?.addEventListener('click', saveEditor);
			element.querySelector('[data-collection-create]')?.addEventListener('click', () => {
				openCollectionItemCreator({
					collection: options.collection,
						onCreated: async () => {
							currentQuery = '';
							currentPage = 1;
							cursorHistory = [''];
						if (input) input.value = '';
						await update();
					}
				});
			});
			let debounce: number | undefined;
			input?.addEventListener('input', () => {
				window.clearTimeout(debounce);
				debounce = window.setTimeout(() => {
					const value = input.value.trim();
					if (value.length > 0 && value.length < MIN_SEARCH_LENGTH) return;
					currentQuery = value;
					currentPage = 1;
					cursorHistory = [''];
					update();
				}, 300);
			});
			update();
		}
	});
};
