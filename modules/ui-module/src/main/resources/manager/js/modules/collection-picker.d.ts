import { CollectionItemSummary } from '@cms/modules/rpc/rpc-collection.js';
export interface CollectionItemPickerOptions {
    collection: string;
    title?: string;
    selectText?: string;
    onSelect: (item: CollectionItemSummary) => void | Promise<void>;
}
export declare const openCollectionItemPicker: (options: CollectionItemPickerOptions) => void;
