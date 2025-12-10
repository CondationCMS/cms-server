import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
export interface RadioFieldOptions extends FieldOptions {
    options?: {
        choices: Array<{
            label: string;
            value: string;
        }>;
    };
}
export declare const RadioField: FormField;
