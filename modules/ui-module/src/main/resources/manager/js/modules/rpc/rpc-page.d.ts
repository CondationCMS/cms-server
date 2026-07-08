export interface CreatePageOptions {
    uri: string;
    name: string;
    contentType: string;
}
export interface CreatePageResponse {
    result: {
        uri?: string;
        error?: string;
    };
}
declare const createPage: (options: CreatePageOptions) => Promise<CreatePageResponse>;
export interface FilterPagesOptions {
    where?: Field[];
    page?: number;
    size?: number;
}
export interface Field {
    field: string;
    operator: string;
    value: any;
}
export interface ItemDto {
    uri: string;
    meta?: any;
}
export interface PageDto {
    totalItems: number;
    pageSize: number;
    totalPages: number;
    page: number;
    items: ItemDto[];
}
export interface FilterPagesResponse {
    result: PageDto;
}
declare const filterPages: (options: FilterPagesOptions) => Promise<FilterPagesResponse>;
declare const deletePage: (options: any) => Promise<any>;
export { createPage, deletePage, filterPages };
