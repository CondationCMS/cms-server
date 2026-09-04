/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
export interface FormDefinition {
    fields: FormFieldDefinition[];
}
export interface FormFieldDefinition {
    type: string;
    name: string;
    title: string;
    required: boolean;
    requiredMessage?: string;
}
export interface PageTemplate {
    name: string;
    template: string;
    forms: Record<string, FormDefinition>;
    contentFolder: string;
    createButton: boolean;
}
export interface SectionEntryTemplate {
    section: string;
    name: string;
    template: string;
    forms: Record<string, FormDefinition>;
}
export interface ListItemType {
    name: string;
    form: FormDefinition;
}
export interface CollectionType {
    name: string;
    label: string;
    forms: Record<string, FormDefinition>;
}
interface ContentTypeResponse<T> {
    result: T[];
}
declare const getSectionEntryTemplates: (options: any) => Promise<ContentTypeResponse<SectionEntryTemplate>>;
declare const getPageTemplates: (options: any) => Promise<ContentTypeResponse<PageTemplate>>;
declare const getListItemTypes: (options: any) => Promise<ContentTypeResponse<ListItemType>>;
declare const getCollectionTypes: () => Promise<ContentTypeResponse<CollectionType>>;
declare const getMediaForm: (options: any) => Promise<any>;
declare const createCSRFToken: (options: any) => Promise<any>;
export declare enum Format {
    WEBP = 0,
    JPEG = 1,
    PNG = 2
}
export interface MediaFormat {
    name: string;
    height?: number;
    width?: number;
    format: Format;
    compression: boolean;
    cropped: boolean;
}
export interface MediaFormatsResponse {
    result: MediaFormat[];
}
declare const getMediaFormats: (options: any) => Promise<MediaFormatsResponse>;
declare const getShortCodeNames: (options: any) => Promise<any>;
export { getSectionEntryTemplates, getPageTemplates, getMediaForm, getShortCodeNames, getMediaFormats, getListItemTypes, getCollectionTypes, createCSRFToken };
