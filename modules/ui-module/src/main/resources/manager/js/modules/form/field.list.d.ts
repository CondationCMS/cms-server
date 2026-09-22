import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface ListFieldOptions extends FieldOptions {
    options: {
        nameField?: string;
    };
}
export declare const ListField: FormField;
