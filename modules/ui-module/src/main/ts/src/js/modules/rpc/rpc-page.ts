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

import { executeRemoteCall } from '@cms/modules/rpc/rpc.js'

export interface CreatePageOptions {
	uri: string; // The URI of the folder where the page should be created
	name: string; // The name of the page to be created
	contentType: string; // Optional content type for the page
}
export interface CreatePageResponse {
	result: {
		uri?: string; // The URI of the created page, if successful
	}
}
const createPage = async (options: CreatePageOptions) : Promise<CreatePageResponse> => {
	var data = {
		method: "page.create",
		parameters: options
	}
	return await executeRemoteCall(data);
};

export interface FilterPagesOptions {
	where?: Field[]; // Optional list of fields to return
	page?: number; // Für Paginierung: Start-Offset
	size?: number; // Für Paginierung: Maximale Anzahl an Ergebnissen
}
export interface Field {
	field: string;
	operator: string;
	value: any;
}
export interface ItemDto { // Renamed from previous PageDto
	uri: string;
	meta?: any;
}
export interface PageDto { // New interface for paginated response
	totalItems: number;
	pageSize: number;
	totalPages: number;
	page: number;
	items: ItemDto[];
}
export interface FilterPagesResponse {
	result: PageDto; // Now returns the paginated PageDto
}
const filterPages = async (options: FilterPagesOptions) : Promise<FilterPagesResponse> => {
	var data = {
		method: "pages.filter",
		parameters: options
	}
	return await executeRemoteCall(data);
}

const deletePage = async (options: any) => {
	var data = {
		method: "page.delete",
		parameters: options
	}
	return await executeRemoteCall(data);
};

export interface SearchPagesOptions {
	query: string;
}
export interface SearchResultDto {
	uri: string;
	title: string;
}
export interface SearchPagesResponse {
	result: SearchResultDto[];
}
const searchPages = async (options: SearchPagesOptions) : Promise<SearchPagesResponse> => {
	var data = {
		method: "pages.search",
		parameters: options
	}
	return await executeRemoteCall(data);
}

export { createPage, deletePage, filterPages, searchPages };
