/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
declare const createForm: (options: any) => Form;
export { createForm };
export interface FormContext {
    formElement: HTMLFormElement | null;
    fields: any[];
}
export interface Form {
    init: (container: Element | string) => void;
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
}
