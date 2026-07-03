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
export interface CreatePageOptions {
    uri: string;
    name: string;
    contentType: string;
}
export interface CreatePageResponse {
    result: {
        uri?: string;
        error?: string;
    };
}
declare const createPage: (options: CreatePageOptions) => Promise<CreatePageResponse>;
export interface FilterPagesOptions {
    where?: Field[];
    page?: number;
    size?: number;
}
export interface Field {
    field: string;
    operator: string;
    value: any;
}
export interface ItemDto {
    uri: string;
    meta?: any;
}
export interface PageDto {
    totalItems: number;
    pageSize: number;
    totalPages: number;
    page: number;
    items: ItemDto[];
}
export interface FilterPagesResponse {
    result: PageDto;
}
declare const filterPages: (options: FilterPagesOptions) => Promise<FilterPagesResponse>;
declare const deletePage: (options: any) => Promise<any>;
export { createPage, deletePage, filterPages };
