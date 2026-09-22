export interface CreatePageOptions {
    uri: string;
    name: string;
    contentType: string;
}
export interface CreatePageResponse {
    result: {
        uri?: string;
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
export interface SearchPagesOptions {
    query: string;
}
export interface SearchResultDto {
    uri: string;
    url: string;
    title: string;
}
export interface SearchPagesResponse {
    result: SearchResultDto[];
}
declare const searchPages: (options: SearchPagesOptions) => Promise<SearchPagesResponse>;
export { createPage, deletePage, filterPages, searchPages };
