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
import { createUser, deleteUser, listRoles, listUsers, updateUser } from '@cms/modules/rpc/rpc-access.js';
const escapeHtml = (value) => value.replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;' }[character] || character));
class UserManager {
    constructor(root) {
        this.root = root;
        this.users = [];
        this.roles = [];
    }
    async show() {
        this.root.innerHTML = '<div class="cms-access-loading"><span class="spinner-border"></span> Loading users…</div>';
        try {
            [this.users, this.roles] = await Promise.all([listUsers(), listRoles()]);
            this.render();
        }
        catch (error) {
            this.fail('Could not load users', error);
        }
    }
    render() {
        this.root.innerHTML = `<section class="cms-access-manager"><header class="cms-access-header"><div><p class="cms-menu-eyebrow">Access control</p>
			<h2>Manager users</h2><p>Create accounts and assign one or more roles.</p></div>
			<button class="btn btn-primary" data-new-user><i class="bi bi-person-plus"></i> New user</button></header>
			<div class="card cms-user-table"><table class="table table-hover align-middle"><thead><tr><th>User</th><th>Email</th><th>Roles</th><th></th></tr></thead>
			<tbody>${this.users.map(user => `<tr data-user="${escapeHtml(user.username)}"><td><strong>${escapeHtml(user.username)}</strong></td>
			<td>${escapeHtml(user.mail || '—')}</td><td>${user.roles.map(role => `<span class="badge text-bg-primary">${escapeHtml(role)}</span>`).join(' ')}</td>
			<td class="text-end"><button class="btn btn-outline-primary btn-sm" data-edit>Edit</button> <button class="btn btn-outline-danger btn-sm" data-delete><i class="bi bi-trash"></i></button></td></tr>`).join('') ||
            '<tr><td colspan="4" class="cms-access-empty">No manager users configured.</td></tr>'}</tbody></table></div><div data-user-editor></div></section>`;
        this.root.querySelector('[data-new-user]')?.addEventListener('click', () => this.editor({ username: '', mail: '', roles: [] }, true));
        this.root.querySelectorAll('[data-edit]').forEach(button => button.addEventListener('click', () => {
            const username = button.closest('[data-user]').dataset.user || '';
            this.editor(this.users.find(user => user.username === username), false);
        }));
        this.root.querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click', async () => {
            const username = button.closest('[data-user]').dataset.user || '';
            if (!confirm(`Delete user “${username}”?`))
                return;
            try {
                await deleteUser(username);
                await this.show();
                this.toast('User deleted', `${username} was deleted.`);
            }
            catch (error) {
                this.fail('Could not delete user', error);
            }
        }));
    }
    editor(user, isNew) {
        const host = this.root.querySelector('[data-user-editor]');
        host.innerHTML = `<div class="cms-access-editor card"><div class="cms-access-editor__heading"><div><p class="cms-menu-eyebrow">${isNew ? 'Create' : 'Edit'}</p>
			<h3>${isNew ? 'New manager user' : escapeHtml(user.username)}</h3></div><button class="btn-close btn-close-white" data-close></button></div>
			<div class="cms-access-fields"><label><span>Username</span><input class="form-control" data-username value="${escapeHtml(user.username)}" ${isNew ? '' : 'disabled'}></label>
			<label><span>Email</span><input class="form-control" type="email" data-mail value="${escapeHtml(user.mail)}"></label>
			<label><span>${isNew ? 'Password' : 'New password (optional)'}</span><input class="form-control" type="password" data-password autocomplete="new-password"></label></div>
			<h4>Roles</h4><div class="cms-permission-list">${this.roles.map(role => `<label class="cms-permission-option"><input class="form-check-input" type="checkbox"
			value="${escapeHtml(role.id)}" ${user.roles.includes(role.id) ? 'checked' : ''}><span><strong>${escapeHtml(role.name)}</strong><small>${escapeHtml(role.id)}</small></span></label>`).join('')}</div>
			<div class="cms-access-editor__actions"><button class="btn btn-secondary" data-close>Cancel</button><button class="btn btn-primary" data-save>Save user</button></div></div>`;
        host.scrollIntoView({ behavior: 'smooth', block: 'start' });
        host.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => host.replaceChildren()));
        host.querySelector('[data-save]')?.addEventListener('click', async () => {
            const username = host.querySelector('[data-username]').value.trim();
            const mail = host.querySelector('[data-mail]').value.trim();
            const password = host.querySelector('[data-password]').value;
            const roles = Array.from(host.querySelectorAll('input[type=checkbox]:checked')).map(input => input.value);
            if (!username || (isNew && !password) || roles.length === 0) {
                this.toast('Incomplete user', 'Username, role and initial password are required.', 'error');
                return;
            }
            try {
                isNew ? await createUser({ username, mail, roles }, password) : await updateUser({ username, mail, roles }, password);
                await this.show();
                this.toast('User saved', `${username} was updated.`);
            }
            catch (error) {
                this.fail('Could not save user', error);
            }
        });
    }
    toast(title, message, type = 'success') { showToast({ title, message, type }); }
    fail(title, error) { this.toast(title, error instanceof Error ? error.message : 'Unknown error', 'error'); }
}
export const runAction = async () => openModal({ title: 'Manage users', fullscreen: true, showFooter: false,
    body: '<div class="cms-access-root"></div>', onShow: (modal) => new UserManager(modal.querySelector('.cms-access-root')).show() });
