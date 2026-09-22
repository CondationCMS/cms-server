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
