import { FieldOptions, FormField } from "@cms/modules/form/forms.js";
declare global {
    interface HTMLInputElement {
        cherryEditor?: any;
    }
}
export interface MarkdownFieldOptions extends FieldOptions {
    placeholder?: string;
    height?: string;
}
export declare const MarkdownField: FormField;
