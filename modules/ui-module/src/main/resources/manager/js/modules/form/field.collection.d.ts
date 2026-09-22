import { FieldOptions, FormField } from '@cms/modules/form/forms.js';
export interface CollectionFieldOptions extends FieldOptions {
    options?: {
        collection?: string;
    };
}
export declare const CollectionField: FormField;
