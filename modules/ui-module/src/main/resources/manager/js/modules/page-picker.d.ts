import { SearchResultDto } from '@cms/modules/rpc/rpc-page.js';
export interface PagePickerOptions {
    title?: string;
    selectText?: string;
    onSelect: (page: SearchResultDto) => void | Promise<void>;
}
export declare const openPagePicker: (options: PagePickerOptions) => void;
