import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface RangeFieldOptions extends FieldOptions {
    options?: {
        min?: number;
        max?: number;
        step?: number;
    };
}
export declare const RangeField: FormField;
