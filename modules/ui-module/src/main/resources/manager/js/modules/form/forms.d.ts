declare const getFormFields: (definition: any) => any[];
declare const createForm: (options: any) => Form;
export { createForm, getFormFields };
export interface FormContext {
    formElement: HTMLFormElement | null;
    fields: any[];
}
export interface Form {
    init: (container: Element | string) => void;
    validate: () => boolean;
    getData: () => any;
    getRawData: () => any;
}
export interface FormField {
    markup: (options: FieldOptions, value?: any) => string;
    init: (context: FormContext) => void;
    data: (context: FormContext) => any;
}
export interface FieldOptions {
    name?: string;
    title?: string;
    required?: boolean;
    requiredMessage?: string;
}
