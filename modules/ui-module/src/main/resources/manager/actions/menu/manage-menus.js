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
import { showToast } from '@cms/modules/toast.js';
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
const uuid = () => {
    if (typeof crypto.randomUUID === 'function') {
        return crypto.randomUUID();
    }
    return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
};
const createItem = (type = 'link') => ({
    id: uuid(),
    type,
    label: type === 'heading' ? 'Neue Überschrift' : type === 'divider' ? 'Trenner' : 'Neuer Menüpunkt',
    url: type === 'link' ? '/' : '',
    target: '_self',
    enabled: true,
    children: []
});
const countItems = (items) => items.reduce((total, item) => total + 1 + countItems(item.children), 0);
const typeMeta = {
    link: { icon: '↗', name: 'Link' },
    heading: { icon: 'T', name: 'Überschrift' },
    divider: { icon: '―', name: 'Trenner' }
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
        this.setTitle('Menüs verwalten');
        this.root.innerHTML = `
			<section class="cms-menu-overview">
				<div class="cms-menu-overview__heading">
					<div>
						<p class="cms-menu-eyebrow">CondationCMS · Navigation</p>
						<h2>Menüs</h2>
						<p>Erstelle und bearbeite die Navigationen dieser Site.</p>
					</div>
					<button class="btn btn-primary" type="button" data-menu-create>
						<i class="bi bi-plus-lg"></i> Neues Menü
					</button>
				</div>
				<div class="cms-menu-create card" data-menu-create-form hidden>
					<div>
						<label class="form-label" for="cms-menu-new-id">Menü-ID</label>
						<input class="form-control" id="cms-menu-new-id" placeholder="z. B. main-navigation"
							pattern="[A-Za-z0-9][A-Za-z0-9_-]*">
						<div class="form-text">Wird als Dateiname unter config/menus verwendet.</div>
					</div>
					<div>
						<label class="form-label" for="cms-menu-new-name">Anzeigename</label>
						<input class="form-control" id="cms-menu-new-name" placeholder="Hauptnavigation">
					</div>
					<div class="d-flex gap-2 align-items-end">
						<button class="btn btn-primary" type="button" data-menu-create-confirm>Editor öffnen</button>
						<button class="btn btn-secondary" type="button" data-menu-create-cancel>Abbrechen</button>
					</div>
				</div>
				<div class="cms-menu-list" data-menu-list>
					<div class="cms-menu-empty"><span class="spinner-border spinner-border-sm"></span> Menüs werden geladen …</div>
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
						<strong>Noch keine Menüs</strong>
						<span>Lege das erste Menü für diese Site an.</span>
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
					<span class="badge text-bg-primary">${countItems(menu.items)} Einträge</span>
					<div class="cms-menu-card__actions">
						<button class="btn btn-outline-primary btn-sm" type="button" data-menu-edit>
							<i class="bi bi-pencil"></i> Bearbeiten
						</button>
						<button class="btn btn-outline-danger btn-sm" type="button" data-menu-delete
							aria-label="${escapeHtml(menu.name)} löschen">
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
            list.innerHTML = '<div class="alert alert-danger">Die Menüs konnten nicht geladen werden.</div>';
            this.toastError('Menüs konnten nicht geladen werden', error);
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
            this.toastError('Menü konnte nicht geladen werden', error);
        }
    }
    async removeMenu(id, card) {
        if (!window.confirm(`Menü „${id}“ wirklich löschen?`)) {
            return;
        }
        try {
            if (await deleteMenu(id)) {
                card.remove();
                showToast({
                    title: 'Menü gelöscht',
                    message: `${id}.yaml wurde gelöscht.`,
                    type: 'success'
                });
                if (!this.root.querySelector('[data-menu-id]')) {
                    await this.loadOverview();
                }
            }
        }
        catch (error) {
            this.toastError('Menü konnte nicht gelöscht werden', error);
        }
    }
    showEditor() {
        if (!this.menu)
            return;
        this.setTitle(`Menü bearbeiten · ${this.menu.name}`);
        this.root.innerHTML = `
			<section class="cms-menu-builder">
				<header class="cms-menu-builder__bar">
					<button class="btn btn-secondary" type="button" data-menu-back>
						<i class="bi bi-arrow-left"></i> Übersicht
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
						<button class="btn btn-secondary" type="button" data-menu-reset>Zurücksetzen</button>
						<button class="btn btn-primary" type="button" data-menu-save>
							<i class="bi bi-floppy"></i> Speichern
						</button>
					</div>
				</header>
				<div class="cms-menu-builder__workspace">
					<aside class="cms-menu-toolbox card">
						<div>
							<p class="cms-menu-eyebrow">Eintrag hinzufügen</p>
							<h3>Bausteine</h3>
						</div>
						<div class="cms-menu-tools">
							${this.toolButton('link', '↗', 'Link', 'Seite oder externe URL')}
							${this.toolButton('heading', 'T', 'Überschrift', 'Gruppiert Menübereiche')}
							${this.toolButton('divider', '―', 'Trenner', 'Optische Abgrenzung')}
						</div>
						<div class="cms-menu-tip">
							<i class="bi bi-stars"></i>
							<span><strong>Tipp</strong> Ziehe Einträge am Griff. Unterpunkte legst du über das Verzweigungs-Symbol an.</span>
						</div>
					</aside>
					<div class="cms-menu-editor card">
						<div class="cms-menu-editor__heading">
							<div>
								<p class="cms-menu-eyebrow">Menüstruktur</p>
								<h3>${escapeHtml(this.menu.name)}</h3>
							</div>
							<span class="badge text-bg-primary" data-menu-count></span>
						</div>
						<div class="cms-menu-columns" aria-hidden="true">
							<span>Menüpunkt</span><span>Typ &amp; Ziel</span><span>Aktionen</span>
						</div>
						<div class="cms-menu-tree cms-menu-sortable" data-menu-root></div>
						<button class="cms-menu-add-main" type="button" data-menu-add-main>
							<span>+</span> Neuen Menüpunkt hinzufügen
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
            `${countItems(this.menu.items)} Einträge`;
        this.setupSortable(root);
    }
    renderItem(item) {
        const element = document.createElement('article');
        element.className = `cms-menu-item${item.enabled ? '' : ' is-disabled'}`;
        element.dataset.id = item.id;
        element.innerHTML = `
			<div class="cms-menu-item__main">
				<button class="cms-menu-drag" type="button" aria-label="Menüpunkt verschieben">
					<i class="bi bi-grip-vertical"></i>
				</button>
				<span class="cms-menu-type">${typeMeta[item.type].icon}</span>
				<div class="cms-menu-item__copy">
					<strong></strong><small></small>
				</div>
				<span class="cms-menu-kind">${typeMeta[item.type].name}</span>
				<div class="cms-menu-item__actions">
					<button class="btn btn-outline-secondary btn-sm" data-add-child title="Unterpunkt hinzufügen">↳</button>
					<button class="btn btn-outline-primary btn-sm" data-edit-item title="Bearbeiten"><i class="bi bi-pencil"></i></button>
					<button class="btn btn-outline-danger btn-sm" data-delete-item title="Löschen"><i class="bi bi-x-lg"></i></button>
				</div>
			</div>
			<form class="cms-menu-item__form" hidden>
				<label><span>Bezeichnung</span><input class="form-control form-control-sm" name="label"></label>
				<label data-url-field><span>URL / Pfad</span><input class="form-control form-control-sm" name="url" placeholder="/beispiel"></label>
				<label><span>Typ</span><select class="form-select form-select-sm" name="type">
					<option value="link">Link</option><option value="heading">Überschrift</option><option value="divider">Trenner</option>
				</select></label>
				<label data-target-field><span>Öffnen in</span><select class="form-select form-select-sm" name="target">
					<option value="_self">Gleichem Fenster</option><option value="_blank">Neuem Fenster</option>
				</select></label>
				<label class="cms-menu-enabled"><input class="form-check-input" name="enabled" type="checkbox"><span>Eintrag aktiv</span></label>
				<button class="btn btn-outline-primary btn-sm" type="submit">Übernehmen</button>
			</form>
			<div class="cms-menu-children" hidden>
				<div class="cms-menu-branch"></div>
				<div class="cms-menu-sortable" data-children-list></div>
			</div>`;
        element.querySelector('.cms-menu-item__copy strong').textContent = item.label;
        const detail = item.type === 'link'
            ? item.url || 'Keine URL'
            : item.type === 'heading' ? `${item.children.length} Unterpunkte` : 'Optische Trennung';
        element.querySelector('.cms-menu-item__copy small').textContent =
            `${detail}${item.enabled ? '' : ' · Deaktiviert'}`;
        const form = element.querySelector('form');
        form.elements.namedItem('label').value = item.label;
        form.elements.namedItem('url').value = item.url;
        form.elements.namedItem('type').value = item.type;
        form.elements.namedItem('target').value = item.target;
        form.elements.namedItem('enabled').checked = item.enabled;
        this.updateFormFields(form, item.type);
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
            item.label = form.elements.namedItem('label').value.trim() || 'Unbenannter Eintrag';
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
            this.setTitle(`Menü bearbeiten · ${this.menu.name}`);
            showToast({
                title: 'Menü gespeichert',
                message: `${this.menu.id}.yaml wurde aktualisiert.`,
                type: 'success'
            });
        }
        catch (error) {
            this.toastError('Menü konnte nicht gespeichert werden', error);
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
            message: error instanceof Error ? error.message : 'Unbekannter Fehler',
            type: 'error'
        });
    }
}
export const runAction = async () => {
    openModal({
        title: 'Menüs verwalten',
        body: '<div class="cms-menu-manager-root"></div>',
        fullscreen: true,
        showFooter: false,
        onShow: (modalElement) => {
            const root = modalElement.querySelector('.cms-menu-manager-root');
            new MenuManager(root, modalElement).showOverview();
        }
    });
};
