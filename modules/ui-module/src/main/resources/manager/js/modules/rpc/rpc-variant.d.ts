export interface VariantDto {
    id: string;
    uri: string;
    url: string;
    meta: Record<string, unknown>;
}
export interface GetVariantsOptions {
    uri: string;
    siteId?: string;
}
export interface GetVariantsResult {
    uri: string;
    canonical: {
        uri: string;
        url: string;
        title: string;
        template: string;
    };
    activeVariantId?: string | null;
    variants: VariantDto[];
}
export interface CreateVariantOptions {
    uri: string;
    id: string;
    title: string;
    template: string;
    copyContent: boolean;
}
export interface CreateVariantResult {
    id: string;
    uri: string;
    url: string;
}
export interface DeleteVariantResult {
    id: string;
    url: string;
}
export interface VariantSelectorDto {
    id: string;
    label: string;
}
export interface GetVariantSelectorsResult {
    selector: string;
    selectors: VariantSelectorDto[];
}
declare const getVariants: (options: GetVariantsOptions) => Promise<GetVariantsResult>;
declare const createVariant: (options: CreateVariantOptions) => Promise<CreateVariantResult>;
declare const deleteVariant: (uri: string, id: string) => Promise<DeleteVariantResult>;
declare const getVariantSelectors: (uri: string) => Promise<GetVariantSelectorsResult>;
declare const setVariantSelector: (uri: string, selector: string) => Promise<{
    selector: string;
}>;
export { createVariant, deleteVariant, getVariants, getVariantSelectors, setVariantSelector };
