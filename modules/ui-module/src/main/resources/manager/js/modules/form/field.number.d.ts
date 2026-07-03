import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface NumberFieldOptions extends FieldOptions {
    options: {
        min?: number;
        max?: number;
        step?: number;
    };
    placeholder?: string;
}
export declare const NumberField: FormField;
