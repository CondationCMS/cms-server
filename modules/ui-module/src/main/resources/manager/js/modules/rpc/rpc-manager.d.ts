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
declare const getSectionTemplates: (options: any) => Promise<any>;
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
declare const getTagNames: (options: any) => Promise<any>;
export { getSectionTemplates, getPageTemplates, getMediaForm, getTagNames, getMediaFormats, getListItemTypes, createCSRFToken };
