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
export declare const listMenus: () => Promise<Menu[]>;
export declare const getMenu: (id: string) => Promise<Menu>;
export declare const createMenu: (menu: Menu) => Promise<Menu>;
export declare const updateMenu: (menu: Menu) => Promise<Menu>;
export declare const deleteMenu: (id: string) => Promise<boolean>;
