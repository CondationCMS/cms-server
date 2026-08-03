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
const call = async (method, parameters = {}) => {
    const response = await executeRemoteCall({ method, parameters });
    return response.result;
};
export const listPermissions = () => call('access.permissions.list');
export const listRoles = () => call('access.roles.list');
export const saveRole = (role) => call('access.roles.save', { role });
export const deleteRole = async (id) => (await call('access.roles.delete', { id })).deleted;
export const listUsers = () => call('access.users.list');
export const createUser = (user, password) => call('access.users.create', { ...user, password });
export const updateUser = (user, password = '') => call('access.users.update', { ...user, password });
export const deleteUser = async (username) => (await call('access.users.delete', { username })).deleted;
