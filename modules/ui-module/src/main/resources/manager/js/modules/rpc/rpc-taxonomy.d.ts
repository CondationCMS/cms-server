export interface TaxonomyValue {
    id: string;
    title: string;
}
declare const getTaxonomies: () => Promise<Record<string, string>>;
declare const getTaxonomyValues: (slug: string) => Promise<TaxonomyValue[]>;
declare const createTaxonomyValue: (slug: string, title: string) => Promise<TaxonomyValue>;
export { getTaxonomies, getTaxonomyValues, createTaxonomyValue };
