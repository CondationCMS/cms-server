import { Form } from '@cms/modules/form/forms.js';
import { CollectionType } from '@cms/modules/rpc/rpc-manager.js';
export declare const collectionForm: (types: CollectionType[], collection: string, mode?: "create" | "edit") => any;
export interface EditCollectionItemOptions {
    collection: string;
    id: string;
    reloadAfterSave?: boolean;
    onSaved?: () => void | Promise<void>;
}
export interface CollectionItemEditor {
    form: Form;
    save: () => Promise<boolean>;
}
export declare const createCollectionItemEditor: (options: EditCollectionItemOptions) => Promise<CollectionItemEditor>;
export declare const openCollectionItemEditor: (options: EditCollectionItemOptions) => Promise<void>;
export declare const runAction: (options: EditCollectionItemOptions) => Promise<void>;
