import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface TagsFieldOptions extends FieldOptions {
    options?: {
        taxonomy: string;
    };
}
export declare const TagsField: FormField;
