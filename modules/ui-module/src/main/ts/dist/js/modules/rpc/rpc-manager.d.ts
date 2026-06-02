declare const getSectionEntryTemplates: (options: any) => Promise<any>;
declare const getPageTemplates: (options: any) => Promise<any>;
declare const getListItemTypes: (options: any) => Promise<any>;
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
export { getSectionEntryTemplates, getPageTemplates, getMediaForm, getShortCodeNames, getMediaFormats, getListItemTypes, createCSRFToken };
