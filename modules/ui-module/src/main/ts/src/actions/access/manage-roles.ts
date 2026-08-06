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
import { openModal } from '@cms/modules/modal.js';
import { showToast } from '@cms/modules/toast.js';
import { deleteRole, listPermissions, listRoles, Permission, Role, saveRole } from '@cms/modules/rpc/rpc-access.js';

const escapeHtml = (value: string) => value.replace(/[&<>'"]/g, character =>
	({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;' }[character] || character));

class RoleManager {
	private roles: Role[] = [];
	private permissions: Permission[] = [];
	constructor(private readonly root: HTMLElement) {}

	async show(): Promise<void> {
		this.root.innerHTML = '<div class="cms-access-loading"><span class="spinner-border"></span> Loading roles…</div>';
		try {
			[this.roles, this.permissions] = await Promise.all([listRoles(), listPermissions()]);
			this.render();
		} catch (error) { this.fail('Could not load roles', error); }
	}

	private render(): void {
		this.root.innerHTML = `<section class="cms-access-manager">
			<header class="cms-access-header"><div><p class="cms-menu-eyebrow">Access control</p><h2>Roles</h2>
			<p>Bundle permissions and assign the roles to manager users.</p></div>
			<button class="btn btn-primary" data-new-role><i class="bi bi-plus-lg"></i> New role</button></header>
			<div class="cms-access-grid" data-role-list></div><div data-role-editor></div></section>`;
		const list = this.root.querySelector('[data-role-list]') as HTMLElement;
		list.innerHTML = this.roles.map(role => `<article class="card cms-access-card" data-role="${escapeHtml(role.id)}">
			<div class="cms-access-card__icon"><i class="bi bi-shield-lock"></i></div><div><strong>${escapeHtml(role.name)}</strong>
			<code>${escapeHtml(role.id)}</code><small>${role.permissions.length} permissions</small></div>
			<button class="btn btn-outline-primary btn-sm" data-edit>Edit</button>
			<button class="btn btn-outline-danger btn-sm" data-delete><i class="bi bi-trash"></i></button></article>`).join('') ||
			'<div class="cms-access-empty">No roles configured.</div>';
		this.root.querySelector('[data-new-role]')?.addEventListener('click', () => this.editor({ id: '', name: '', permissions: [] }, true));
		list.querySelectorAll<HTMLElement>('[data-edit]').forEach(button => button.addEventListener('click', () => {
			const id = (button.closest('[data-role]') as HTMLElement).dataset.role || '';
			this.editor(this.roles.find(role => role.id === id)!, false);
		}));
		list.querySelectorAll<HTMLElement>('[data-delete]').forEach(button => button.addEventListener('click', async () => {
			const id = (button.closest('[data-role]') as HTMLElement).dataset.role || '';
			if (!confirm(`Delete role “${id}”?`)) return;
			try { await deleteRole(id); await this.show(); this.toast('Role deleted', `${id} was deleted.`); }
			catch (error) { this.fail('Could not delete role', error); }
		}));
	}

	private editor(role: Role, isNew: boolean): void {
		const host = this.root.querySelector('[data-role-editor]') as HTMLElement;
		host.innerHTML = `<div class="cms-access-editor card"><div class="cms-access-editor__heading"><div><p class="cms-menu-eyebrow">${isNew ? 'Create' : 'Edit'}</p>
			<h3>${isNew ? 'New role' : escapeHtml(role.name)}</h3></div><button class="btn-close btn-close-white" data-close></button></div>
			<div class="cms-access-fields"><label><span>Role ID</span><input class="form-control" data-id value="${escapeHtml(role.id)}" ${isNew ? '' : 'disabled'} pattern="[a-z][a-z0-9_-]*"></label>
			<label><span>Display name</span><input class="form-control" data-name value="${escapeHtml(role.name)}"></label></div>
			<h4>Permissions</h4><div class="cms-permission-list">${this.permissions.map(permission => `<label class="cms-permission-option">
			<input class="form-check-input" type="checkbox" value="${escapeHtml(permission.key)}" ${role.permissions.includes(permission.key) ? 'checked' : ''}>
			<span><strong>${escapeHtml(permission.key)}</strong><small>${escapeHtml(permission.description)}</small></span></label>`).join('')}</div>
			<div class="cms-access-editor__actions"><button class="btn btn-secondary" data-close>Cancel</button><button class="btn btn-primary" data-save>Save role</button></div></div>`;
		host.scrollIntoView({ behavior: 'smooth', block: 'start' });
		host.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => host.replaceChildren()));
		host.querySelector('[data-save]')?.addEventListener('click', async () => {
			const id = (host.querySelector('[data-id]') as HTMLInputElement).value.trim().toLowerCase();
			const name = (host.querySelector('[data-name]') as HTMLInputElement).value.trim();
			if (!/^[a-z][a-z0-9_-]*$/.test(id) || !name) { this.toast('Invalid role', 'Enter a valid ID and display name.', 'error'); return; }
			const permissions = Array.from(host.querySelectorAll<HTMLInputElement>('input[type=checkbox]:checked')).map(input => input.value);
			try { await saveRole({ id, name, permissions }); await this.show(); this.toast('Role saved', `${name} was updated.`); }
			catch (error) { this.fail('Could not save role', error); }
		});
	}

	private toast(title: string, message: string, type: 'success' | 'error' = 'success') { showToast({ title, message, type }); }
	private fail(title: string, error: unknown) { this.toast(title, error instanceof Error ? error.message : 'Unknown error', 'error'); }
}

export const runAction = async (): Promise<void> => openModal({ title: 'Manage roles', fullscreen: true, showFooter: false,
	body: '<div class="cms-access-root"></div>', onShow: (modal: HTMLElement) => new RoleManager(modal.querySelector('.cms-access-root') as HTMLElement).show() });
