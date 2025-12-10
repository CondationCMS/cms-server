import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface ReferenceFieldOptions extends FieldOptions {
    options?: {
        siteid?: string;
    };
}
export declare const ReferenceField: FormField;
