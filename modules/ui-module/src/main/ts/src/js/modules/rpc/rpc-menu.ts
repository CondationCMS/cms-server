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

import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';

export type MenuItemType = 'link' | 'heading' | 'divider';

export interface MenuItem {
	id: string;
	type: MenuItemType;
	label: string;
	url: string;
	target: '_self' | '_blank';
	enabled: boolean;
	children: MenuItem[];
}

export interface Menu {
	id: string;
	name: string;
	items: MenuItem[];
}

export const listMenus = async (): Promise<Menu[]> => {
	const response = await executeRemoteCall({
		method: 'menu.list',
		parameters: {}
	});
	return response.result || [];
};

export const getMenu = async (id: string): Promise<Menu> => {
	const response = await executeRemoteCall({
		method: 'menu.get',
		parameters: { id }
	});
	return response.result;
};

export const createMenu = async (menu: Menu): Promise<Menu> => {
	const response = await executeRemoteCall({
		method: 'menu.create',
		parameters: { menu }
	});
	return response.result;
};

export const updateMenu = async (menu: Menu): Promise<Menu> => {
	const response = await executeRemoteCall({
		method: 'menu.update',
		parameters: { menu }
	});
	return response.result;
};

export const deleteMenu = async (id: string): Promise<boolean> => {
	const response = await executeRemoteCall({
		method: 'menu.delete',
		parameters: { id }
	});
	return response.result?.deleted === true;
};
