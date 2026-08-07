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
import { openModal } from '@cms/modules/modal.js';
import { openFileBrowser } from '@cms/modules/filebrowser/filebrowser.js';
import { openPagePicker } from '@cms/modules/page-picker.js';
import { showToast } from '@cms/modules/toast.js';
import { uuid } from '@cms/modules/utils.js';
import { createMenu, deleteMenu, getMenu, listMenus, updateMenu } from '@cms/modules/rpc/rpc-menu.js';
// @ts-ignore SortableJS is loaded as an ESM dependency from the same CDN used by the manager.
import Sortable from 'https://cdn.jsdelivr.net/npm/sortablejs@1.15.6/+esm';
const clone = (value) => JSON.parse(JSON.stringify(value));
const escapeHtml = (value) => value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
const createItem = (type = 'link') => ({
    id: uuid(),
    type,
    label: type === 'heading' ? 'New heading' : type === 'divider' ? 'Divider' : 'New menu item',
    url: type === 'link' ? '/' : '',
    target: '_self',
    enabled: true,
    children: []
});
const countItems = (items) => items.reduce((total, item) => total + 1 + countItems(item.children), 0);
const typeMeta = {
    link: { icon: '↗', name: 'Link' },
    heading: { icon: 'T', name: 'Heading' },
    divider: { icon: '―', name: 'Divider' }
};
class MenuManager {
    constructor(root, modalElement) {
        this.menu = null;
        this.originalMenu = null;
        this.isNew = false;
        this.sortableElements = new WeakSet();
        this.root = root;
        this.modalElement = modalElement;
    }
    async showOverview() {
        this.setTitle('Manage menus');
        this.root.innerHTML = `
			<section class="cms-menu-overview">
				<div class="cms-menu-overview__heading">
					<div>
						<p class="cms-menu-eyebrow">CondationCMS · Navigation</p>
						<h2>Menus</h2>
						<p>Create and edit navigation menus for this site.</p>
					</div>
					<button class="btn btn-primary" type="button" data-menu-create>
						<i class="bi bi-plus-lg"></i> New menu
					</button>
				</div>
				<div class="cms-menu-create card" data-menu-create-form hidden>
					<div>
						<label class="form-label" for="cms-menu-new-id">Menu ID</label>
						<input class="form-control" id="cms-menu-new-id" placeholder="e.g. main-navigation"
							pattern="[A-Za-z0-9][A-Za-z0-9_-]*">
						<div class="form-text">Used as the filename under config/menus.</div>
					</div>
					<div>
						<label class="form-label" for="cms-menu-new-name">Display name</label>
						<input class="form-control" id="cms-menu-new-name" placeholder="Main navigation">
					</div>
					<div class="d-flex gap-2 align-items-end">
						<button class="btn btn-primary" type="button" data-menu-create-confirm>Open editor</button>
						<button class="btn btn-secondary" type="button" data-menu-create-cancel>Cancel</button>
					</div>
				</div>
				<div class="cms-menu-list" data-menu-list>
					<div class="cms-menu-empty"><span class="spinner-border spinner-border-sm"></span> Loading menus…</div>
				</div>
			</section>`;
        this.root.querySelector('[data-menu-create]')?.addEventListener('click', () => {
            const form = this.root.querySelector('[data-menu-create-form]');
            form.hidden = false;
            this.root.querySelector('#cms-menu-new-id').focus();
        });
        this.root.querySelector('[data-menu-create-cancel]')?.addEventListener('click', () => {
            this.root.querySelector('[data-menu-create-form]').hidden = true;
        });
        this.root.querySelector('[data-menu-create-confirm]')?.addEventListener('click', () => this.openNewMenu());
        await this.loadOverview();
    }
    async loadOverview() {
        const list = this.root.querySelector('[data-menu-list]');
        try {
            const menus = await listMenus();
            if (menus.length === 0) {
                list.innerHTML = `
					<div class="cms-menu-empty">
						<i class="bi bi-list-nested"></i>
						<strong>No menus yet</strong>
						<span>Create the first menu for this site.</span>
					</div>`;
                return;
            }
            list.innerHTML = menus.map(menu => `
				<article class="cms-menu-card card" data-menu-id="${escapeHtml(menu.id)}">
					<div class="cms-menu-card__icon"><i class="bi bi-list-nested"></i></div>
					<div class="cms-menu-card__copy">
						<strong>${escapeHtml(menu.name)}</strong>
						<code>${escapeHtml(menu.id)}.yaml</code>
					</div>
					<span class="badge text-bg-primary">${countItems(menu.items)} items</span>
					<div class="cms-menu-card__actions">
						<button class="btn btn-outline-primary btn-sm" type="button" data-menu-edit>
							<i class="bi bi-pencil"></i> Edit
						</button>
						<button class="btn btn-outline-danger btn-sm" type="button" data-menu-delete
							aria-label="Delete ${escapeHtml(menu.name)}">
							<i class="bi bi-trash"></i>
						</button>
					</div>
				</article>`).join('');
            list.querySelectorAll('[data-menu-edit]').forEach(button => {
                button.addEventListener('click', () => {
                    const id = button.closest('[data-menu-id]').dataset.menuId || '';
                    this.openExistingMenu(id);
                });
            });
            list.querySelectorAll('[data-menu-delete]').forEach(button => {
                button.addEventListener('click', () => {
                    const card = button.closest('[data-menu-id]');
                    this.removeMenu(card.dataset.menuId || '', card);
                });
            });
        }
        catch (error) {
            list.innerHTML = '<div class="alert alert-danger">Could not load menus.</div>';
            this.toastError('Could not load menus', error);
        }
    }
    openNewMenu() {
        const idInput = this.root.querySelector('#cms-menu-new-id');
        const nameInput = this.root.querySelector('#cms-menu-new-name');
        const id = idInput.value.trim();
        if (!/^[A-Za-z0-9][A-Za-z0-9_-]*$/.test(id)) {
            idInput.classList.add('is-invalid');
            idInput.focus();
            return;
        }
        this.isNew = true;
        this.menu = { id, name: nameInput.value.trim() || id, items: [] };
        this.originalMenu = clone(this.menu);
        this.showEditor();
    }
    async openExistingMenu(id) {
        try {
            this.menu = await getMenu(id);
            this.originalMenu = clone(this.menu);
            this.isNew = false;
            this.showEditor();
        }
        catch (error) {
            this.toastError('Could not load menu', error);
        }
    }
    async removeMenu(id, card) {
        if (!window.confirm(`Delete menu “${id}”?`)) {
            return;
        }
        try {
            if (await deleteMenu(id)) {
                card.remove();
                showToast({
                    title: 'Menu deleted',
                    message: `${id}.yaml was deleted.`,
                    type: 'success'
                });
                if (!this.root.querySelector('[data-menu-id]')) {
                    await this.loadOverview();
                }
            }
        }
        catch (error) {
            this.toastError('Could not delete menu', error);
        }
    }
    showEditor() {
        if (!this.menu)
            return;
        this.setTitle(`Edit menu · ${this.menu.name}`);
        this.root.innerHTML = `
			<section class="cms-menu-builder">
				<header class="cms-menu-builder__bar">
					<button class="btn btn-secondary" type="button" data-menu-back>
						<i class="bi bi-arrow-left"></i> Overview
					</button>
					<div class="cms-menu-builder__identity">
						<label>
							<span>Name</span>
							<input class="form-control form-control-sm" data-menu-name value="${escapeHtml(this.menu.name)}">
						</label>
						<code>${escapeHtml(this.menu.id)}.yaml</code>
					</div>
					<div class="cms-menu-builder__actions">
						<button class="btn btn-outline-secondary" type="button" data-menu-json>
							<span aria-hidden="true">{ }</span> JSON
						</button>
						<button class="btn btn-secondary" type="button" data-menu-reset>Reset</button>
						<button class="btn btn-primary" type="button" data-menu-save>
							<i class="bi bi-floppy"></i> Save
						</button>
					</div>
				</header>
				<div class="cms-menu-builder__workspace">
					<aside class="cms-menu-toolbox card">
						<div>
							<p class="cms-menu-eyebrow">Add item</p>
							<h3>Components</h3>
						</div>
						<div class="cms-menu-tools">
							${this.toolButton('link', '↗', 'Link', 'Page or external URL')}
							${this.toolButton('heading', 'T', 'Heading', 'Groups menu sections')}
							${this.toolButton('divider', '―', 'Divider', 'Visual separator')}
						</div>
						<div class="cms-menu-tip">
							<i class="bi bi-stars"></i>
							<span><strong>Tip</strong> Drag items by the handle. Add child items using the branch icon.</span>
						</div>
					</aside>
					<div class="cms-menu-editor card">
						<div class="cms-menu-editor__heading">
							<div>
								<p class="cms-menu-eyebrow">Menu structure</p>
								<h3>${escapeHtml(this.menu.name)}</h3>
							</div>
							<span class="badge text-bg-primary" data-menu-count></span>
						</div>
						<div class="cms-menu-columns" aria-hidden="true">
							<span>Menu item</span><span>Type &amp; target</span><span>Actions</span>
						</div>
						<div class="cms-menu-tree cms-menu-sortable" data-menu-root></div>
						<button class="cms-menu-add-main" type="button" data-menu-add-main>
							<span>+</span> Add new menu item
						</button>
						<pre class="cms-menu-json" data-menu-json-output hidden></pre>
					</div>
				</div>
			</section>`;
        this.root.querySelector('[data-menu-back]')?.addEventListener('click', () => this.showOverview());
        this.root.querySelector('[data-menu-reset]')?.addEventListener('click', () => {
            if (this.originalMenu) {
                this.menu = clone(this.originalMenu);
                this.showEditor();
            }
        });
        this.root.querySelector('[data-menu-save]')?.addEventListener('click', () => this.save());
        this.root.querySelector('[data-menu-json]')?.addEventListener('click', () => this.toggleJson());
        this.root.querySelector('[data-menu-add-main]')?.addEventListener('click', () => {
            this.menu?.items.push(createItem());
            this.renderMenu();
        });
        this.root.querySelectorAll('[data-menu-add-type]').forEach(button => {
            button.addEventListener('click', () => {
                this.menu?.items.push(createItem(button.dataset.menuAddType));
                this.renderMenu();
            });
        });
        this.renderMenu();
    }
    toolButton(type, icon, name, detail) {
        return `
			<button class="cms-menu-tool" type="button" data-menu-add-type="${type}">
				<span class="cms-menu-tool__icon">${icon}</span>
				<span><strong>${name}</strong><small>${detail}</small></span>
				<span class="cms-menu-tool__plus">+</span>
			</button>`;
    }
    renderMenu() {
        if (!this.menu)
            return;
        const root = this.root.querySelector('[data-menu-root]');
        root.replaceChildren(...this.menu.items.map(item => this.renderItem(item)));
        this.root.querySelector('[data-menu-count]').textContent =
            `${countItems(this.menu.items)} items`;
        this.setupSortable(root);
    }
    renderItem(item) {
        const element = document.createElement('article');
        element.className = `cms-menu-item${item.enabled ? '' : ' is-disabled'}`;
        element.dataset.id = item.id;
        element.innerHTML = `
			<div class="cms-menu-item__main">
				<button class="cms-menu-drag" type="button" aria-label="Move menu item">
					<i class="bi bi-grip-vertical"></i>
				</button>
				<span class="cms-menu-type">${typeMeta[item.type].icon}</span>
				<div class="cms-menu-item__copy">
					<strong></strong><small></small>
				</div>
				<span class="cms-menu-kind">${typeMeta[item.type].name}</span>
				<div class="cms-menu-item__actions">
					<button class="btn btn-outline-secondary btn-sm" data-add-child title="Add child item">↳</button>
					<button class="btn btn-outline-primary btn-sm" data-edit-item title="Edit"><i class="bi bi-pencil"></i></button>
					<button class="btn btn-outline-danger btn-sm" data-delete-item title="Delete"><i class="bi bi-x-lg"></i></button>
				</div>
			</div>
			<form class="cms-menu-item__form" hidden>
				<label><span>Label</span><input class="form-control form-control-sm" name="label"></label>
				<label data-url-field><span>URL / path</span><span class="input-group input-group-sm">
					<input class="form-control" name="url" placeholder="/example">
					<button class="btn btn-outline-primary" type="button" data-select-page title="Search pages"
						aria-label="Search pages">
						<i class="bi bi-search"></i>
					</button>
					<button class="btn btn-outline-primary" type="button" data-browse-page title="Open content browser"
						aria-label="Open content browser">
						<i class="bi bi-folder2-open"></i>
					</button>
				</span></label>
				<label><span>Type</span><select class="form-select form-select-sm" name="type">
					<option value="link">Link</option><option value="heading">Heading</option><option value="divider">Divider</option>
				</select></label>
				<label data-target-field><span>Open in</span><select class="form-select form-select-sm" name="target">
					<option value="_self">Same window</option><option value="_blank">New window</option>
				</select></label>
				<label class="cms-menu-enabled"><input class="form-check-input" name="enabled" type="checkbox"><span>Item enabled</span></label>
				<button class="btn btn-outline-primary btn-sm" type="submit">Apply</button>
			</form>
			<div class="cms-menu-children" hidden>
				<div class="cms-menu-branch"></div>
				<div class="cms-menu-sortable" data-children-list></div>
			</div>`;
        element.querySelector('.cms-menu-item__copy strong').textContent = item.label;
        const detail = item.type === 'link'
            ? item.url || 'No URL'
            : item.type === 'heading' ? `${item.children.length} child items` : 'Visual separator';
        element.querySelector('.cms-menu-item__copy small').textContent =
            `${detail}${item.enabled ? '' : ' · Disabled'}`;
        const form = element.querySelector('form');
        form.elements.namedItem('label').value = item.label;
        form.elements.namedItem('url').value = item.url;
        form.elements.namedItem('type').value = item.type;
        form.elements.namedItem('target').value = item.target;
        form.elements.namedItem('enabled').checked = item.enabled;
        this.updateFormFields(form, item.type);
        element.querySelector('[data-select-page]')?.addEventListener('click', () => {
            openPagePicker({
                title: 'Select internal page',
                onSelect: page => {
                    const urlInput = form.elements.namedItem('url');
                    const labelInput = form.elements.namedItem('label');
                    urlInput.value = page.url;
                    if (page.title && (!labelInput.value.trim() || labelInput.value === 'New menu item')) {
                        labelInput.value = page.title;
                    }
                }
            });
        });
        element.querySelector('[data-browse-page]')?.addEventListener('click', () => {
            openFileBrowser({
                title: 'Select internal page',
                type: 'content',
                filter: (file) => file.directory || file.content,
                onSelect: (file) => {
                    if (!file.url)
                        return;
                    const urlInput = form.elements.namedItem('url');
                    const labelInput = form.elements.namedItem('label');
                    urlInput.value = file.url;
                    if (file.title && (!labelInput.value.trim() || labelInput.value === 'New menu item')) {
                        labelInput.value = file.title;
                    }
                }
            });
        });
        form.elements.namedItem('type').addEventListener('change', event => {
            this.updateFormFields(form, event.target.value);
        });
        element.querySelector('[data-edit-item]')?.addEventListener('click', () => {
            form.hidden = !form.hidden;
            if (!form.hidden)
                form.elements.namedItem('label').focus();
        });
        form.addEventListener('submit', event => {
            event.preventDefault();
            item.label = form.elements.namedItem('label').value.trim() || 'Unnamed item';
            item.type = form.elements.namedItem('type').value;
            item.url = item.type === 'link' ? form.elements.namedItem('url').value.trim() : '';
            item.target = item.type === 'link'
                ? form.elements.namedItem('target').value
                : '_self';
            item.enabled = form.elements.namedItem('enabled').checked;
            this.renderMenu();
        });
        element.querySelector('[data-delete-item]')?.addEventListener('click', () => {
            if (this.menu)
                this.removeItem(this.menu.items, item.id);
            this.renderMenu();
        });
        element.querySelector('[data-add-child]')?.addEventListener('click', () => {
            item.children.push(createItem());
            this.renderMenu();
        });
        const childrenWrap = element.querySelector('.cms-menu-children');
        const childrenList = element.querySelector('[data-children-list]');
        if (item.children.length) {
            childrenWrap.hidden = false;
            childrenList.replaceChildren(...item.children.map(child => this.renderItem(child)));
        }
        queueMicrotask(() => this.setupSortable(childrenList));
        return element;
    }
    updateFormFields(form, type) {
        form.querySelector('[data-url-field]').hidden = type !== 'link';
        form.querySelector('[data-target-field]').hidden = type !== 'link';
    }
    setupSortable(listElement) {
        if (this.sortableElements.has(listElement)) {
            return;
        }
        this.sortableElements.add(listElement);
        Sortable.create(listElement, {
            group: 'cms-menu-builder',
            animation: 180,
            fallbackOnBody: true,
            swapThreshold: 0.65,
            handle: '.cms-menu-drag',
            ghostClass: 'cms-menu-sortable-ghost',
            chosenClass: 'cms-menu-sortable-chosen',
            onStart: () => {
                this.root.classList.add('is-dragging-menu-item');
                this.root.querySelectorAll('.cms-menu-children').forEach(wrap => wrap.hidden = false);
            },
            onMove: (event) => !event.dragged.contains(event.to),
            onAdd: (event) => {
                if (!this.menu)
                    return;
                const movedItem = this.removeItem(this.menu.items, event.item.dataset.id);
                if (!movedItem)
                    return;
                const target = this.getListForElement(event.to);
                target.splice(event.newIndex, 0, movedItem);
                this.syncOrderFromDom(event.to);
                this.renderMenu();
            },
            onUpdate: (event) => {
                this.syncOrderFromDom(event.to);
                this.renderMenu();
            },
            onEnd: () => {
                this.root.classList.remove('is-dragging-menu-item');
                this.root.querySelectorAll('.cms-menu-children').forEach(wrap => {
                    const list = wrap.querySelector('[data-children-list]');
                    wrap.hidden = list.children.length === 0;
                });
            }
        });
    }
    getListForElement(listElement) {
        if (listElement.hasAttribute('data-menu-root'))
            return this.menu?.items || [];
        const parent = listElement.closest('.cms-menu-item');
        return this.findItem(this.menu?.items || [], parent?.dataset.id || '')?.children || [];
    }
    syncOrderFromDom(listElement) {
        const target = this.getListForElement(listElement);
        const ids = Array.from(listElement.children)
            .filter(element => element.classList.contains('cms-menu-item'))
            .map(element => element.dataset.id);
        target.sort((left, right) => ids.indexOf(left.id) - ids.indexOf(right.id));
    }
    findItem(items, id) {
        for (const item of items) {
            if (item.id === id)
                return item;
            const nested = this.findItem(item.children, id);
            if (nested)
                return nested;
        }
        return null;
    }
    removeItem(items, id) {
        const index = items.findIndex(item => item.id === id);
        if (index >= 0)
            return items.splice(index, 1)[0];
        for (const item of items) {
            const removed = this.removeItem(item.children, id);
            if (removed)
                return removed;
        }
        return null;
    }
    toggleJson() {
        if (!this.menu)
            return;
        this.readName();
        const output = this.root.querySelector('[data-menu-json-output]');
        output.textContent = JSON.stringify(this.menu, null, 2);
        output.hidden = !output.hidden;
    }
    readName() {
        if (!this.menu)
            return;
        const input = this.root.querySelector('[data-menu-name]');
        this.menu.name = input.value.trim() || this.menu.id;
    }
    async save() {
        if (!this.menu)
            return;
        this.readName();
        const saveButton = this.root.querySelector('[data-menu-save]');
        saveButton.disabled = true;
        try {
            this.menu = this.isNew ? await createMenu(this.menu) : await updateMenu(this.menu);
            this.originalMenu = clone(this.menu);
            this.isNew = false;
            this.setTitle(`Edit menu · ${this.menu.name}`);
            showToast({
                title: 'Menu saved',
                message: `${this.menu.id}.yaml was updated.`,
                type: 'success'
            });
        }
        catch (error) {
            this.toastError('Could not save menu', error);
        }
        finally {
            saveButton.disabled = false;
        }
    }
    setTitle(title) {
        const titleElement = this.modalElement.querySelector('.modal-title');
        if (titleElement)
            titleElement.textContent = title;
    }
    toastError(title, error) {
        showToast({
            title,
            message: error instanceof Error ? error.message : 'Unknown error',
            type: 'error'
        });
    }
}
export const runAction = async () => {
    openModal({
        title: 'Manage menus',
        body: '<div class="cms-menu-manager-root"></div>',
        fullscreen: true,
        showFooter: false,
        onShow: (modalElement) => {
            const root = modalElement.querySelector('.cms-menu-manager-root');
            new MenuManager(root, modalElement).showOverview();
        }
    });
};
