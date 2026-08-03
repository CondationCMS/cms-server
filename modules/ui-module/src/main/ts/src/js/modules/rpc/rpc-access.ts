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
import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';

export interface Permission { key: string; description: string; }
export interface Role { id: string; name: string; permissions: string[]; }
export interface ManagerUser { username: string; mail: string; roles: string[]; }

const call = async <T>(method: string, parameters: Record<string, unknown> = {}): Promise<T> => {
	const response = await executeRemoteCall({ method, parameters });
	return response.result as T;
};

export const listPermissions = () => call<Permission[]>('access.permissions.list');
export const listRoles = () => call<Role[]>('access.roles.list');
export const saveRole = (role: Role) => call<Role>('access.roles.save', { role });
export const deleteRole = async (id: string) => (await call<{deleted: boolean}>('access.roles.delete', { id })).deleted;
export const listUsers = () => call<ManagerUser[]>('access.users.list');
export const createUser = (user: ManagerUser, password: string) =>
	call<ManagerUser>('access.users.create', { ...user, password });
export const updateUser = (user: ManagerUser, password = '') =>
	call<ManagerUser>('access.users.update', { ...user, password });
export const deleteUser = async (username: string) =>
	(await call<{deleted: boolean}>('access.users.delete', { username })).deleted;
