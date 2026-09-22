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
export declare const listCollectionItems: (options: ListCollectionItemsOptions) => Promise<CollectionItemsPage>;
export declare const listCollectionItemsCursor: (options: ListCollectionItemsCursorOptions) => Promise<CollectionItemsCursorPage>;
export declare const getCollectionItem: (collection: string, id: string) => Promise<EditableCollectionItem>;
export declare const saveCollectionItem: (options: {
    collection: string;
    id: string;
    content: any;
    meta: Record<string, any>;
}) => Promise<void>;
export declare const createCollectionItem: (options: {
    collection: string;
    id: string;
    content: any;
    meta: Record<string, any>;
}) => Promise<CollectionItemSummary>;
export declare const deleteCollectionItem: (collection: string, id: string) => Promise<void>;
