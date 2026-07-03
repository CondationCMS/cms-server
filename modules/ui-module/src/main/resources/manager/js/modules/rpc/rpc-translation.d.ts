export interface GetTranslationsOptions {
    uri: string;
}
export interface TranslationDto {
    site: string;
    lang: string;
    country: string;
    url?: string;
    managerDeepLink?: string;
}
declare const getTranslations: (options: GetTranslationsOptions) => Promise<{
    translations: TranslationDto[];
}>;
export interface AddTranslationOptions {
    uri: string;
    language: string;
    translationUri: string;
}
export interface AddTranslationResult {
    uri: string;
    error?: boolean;
}
declare const addTranslation: (options: AddTranslationOptions) => Promise<AddTranslationResult>;
export interface RemoveTranslationOptions {
    uri: string;
    lang: string;
}
export interface RemoveTranslationResult {
    uri: string;
    error?: boolean;
}
declare const removeTranslation: (options: RemoveTranslationOptions) => Promise<RemoveTranslationResult>;
export { getTranslations, addTranslation, removeTranslation };
