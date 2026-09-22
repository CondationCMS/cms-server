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
