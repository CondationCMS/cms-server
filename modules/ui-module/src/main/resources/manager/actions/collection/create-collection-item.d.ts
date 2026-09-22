import { CollectionItemSummary } from '@cms/modules/rpc/rpc-collection.js';
export interface CreateCollectionItemOptions {
    collection: string;
    onCreated?: (item: CollectionItemSummary) => void | Promise<void>;
}
export declare const openCollectionItemCreator: (options: CreateCollectionItemOptions) => Promise<void>;
export declare const runAction: (options: CreateCollectionItemOptions) => Promise<void>;
