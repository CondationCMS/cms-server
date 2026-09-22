export interface AlternateDto {
    site: string;
    locale: string;
    url?: string;
    managerDeepLink?: string;
}
declare const getAlternates: (options: {
    uri: string;
}) => Promise<{
    alternates: AlternateDto[];
}>;
declare const addAlternate: (options: {
    uri: string;
    targetSite: string;
    alternateUri: string;
}) => Promise<{
    uri: string;
}>;
declare const removeAlternate: (options: {
    uri: string;
    targetSite: string;
}) => Promise<{
    uri: string;
}>;
export { getAlternates, addAlternate, removeAlternate };
