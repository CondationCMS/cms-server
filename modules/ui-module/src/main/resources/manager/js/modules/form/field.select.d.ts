import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface SelectFieldOptions extends FieldOptions {
    options?: {
        choices?: Array<string | {
            label: string;
            value: string;
        }>;
    };
}
export declare const SelectField: FormField;
