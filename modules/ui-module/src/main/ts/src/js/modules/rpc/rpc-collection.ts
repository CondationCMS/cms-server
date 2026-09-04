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

export interface CollectionItemSummary {
	id: string;
	collection: string;
	path: string;
	title: string;
	detailUrl?: string | null;
	meta: Record<string, any>;
}


export interface EditableCollectionItem {
	id: string;
	collection: string;
	path: string;
	content: string;
	meta: Record<string, any>;
}

export interface CollectionItemsPage {
	totalItems: number;
	pageSize: number;
	totalPages: number;
	page: number;
	items: CollectionItemSummary[];
}

export interface CollectionItemsCursorPage {
	items: CollectionItemSummary[];
	nextCursor?: string | null;
}

export interface ListCollectionItemsOptions {
	collection: string;
	query?: string;
	page?: number;
	size?: number;
}

export interface ListCollectionItemsCursorOptions {
	collection: string;
	query?: string;
	cursor?: string;
	size?: number;
}

export const listCollectionItems = async (
	options: ListCollectionItemsOptions
): Promise<CollectionItemsPage> => {
	return (await executeRemoteCall({
		method: 'collections.items',
		parameters: options
	})).result as CollectionItemsPage;
};

export const listCollectionItemsCursor = async (
	options: ListCollectionItemsCursorOptions
): Promise<CollectionItemsCursorPage> => {
	return (await executeRemoteCall({
		method: 'collections.items.cursor',
		parameters: options
	})).result as CollectionItemsCursorPage;
};

export const getCollectionItem = async (
	collection: string,
	id: string
): Promise<EditableCollectionItem> => {
	return (await executeRemoteCall({
		method: 'collections.item.get',
		parameters: { collection, id }
	})).result as EditableCollectionItem;
};

export const saveCollectionItem = async (options: {
	collection: string;
	id: string;
	content: any;
	meta: Record<string, any>;
}): Promise<void> => {
	await executeRemoteCall({
		method: 'collections.item.save',
		parameters: options
	});
};

export const createCollectionItem = async (options: {
	collection: string;
	id: string;
	content: any;
	meta: Record<string, any>;
}): Promise<CollectionItemSummary> => {
	return (await executeRemoteCall({
		method: 'collections.item.create',
		parameters: options
	})).result as CollectionItemSummary;
};

export const deleteCollectionItem = async (collection: string, id: string): Promise<void> => {
	await executeRemoteCall({
		method: 'collections.item.delete',
		parameters: { collection, id }
	});
};
