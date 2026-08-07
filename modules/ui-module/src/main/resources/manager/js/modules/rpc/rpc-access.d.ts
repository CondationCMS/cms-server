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
export interface Permission {
    key: string;
    description: string;
}
export interface Role {
    id: string;
    name: string;
    permissions: string[];
}
export interface ManagerUser {
    username: string;
    mail: string;
    roles: string[];
}
export declare const listPermissions: () => Promise<Permission[]>;
export declare const listRoles: () => Promise<Role[]>;
export declare const saveRole: (role: Role) => Promise<Role>;
export declare const deleteRole: (id: string) => Promise<boolean>;
export declare const listUsers: () => Promise<ManagerUser[]>;
export declare const createUser: (user: ManagerUser, password: string) => Promise<ManagerUser>;
export declare const updateUser: (user: ManagerUser, password?: string) => Promise<ManagerUser>;
export declare const deleteUser: (username: string) => Promise<boolean>;
