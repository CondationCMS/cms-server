import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface CheckboxOptions extends FieldOptions {
    key?: string;
    options?: {
        choices: Array<{
            label: string;
            value: string;
        }>;
    };
}
export declare const CheckboxField: FormField;
