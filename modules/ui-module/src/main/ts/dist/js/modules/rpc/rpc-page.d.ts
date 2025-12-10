export interface PageCreateOptions {
    uri: string;
    name: string;
    contentType: string;
}
declare const createPage: (options: PageCreateOptions) => Promise<any>;
declare const deletePage: (options: any) => Promise<any>;
export { createPage, deletePage };
