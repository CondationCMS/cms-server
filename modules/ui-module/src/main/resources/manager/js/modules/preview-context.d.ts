export interface ActivePreviewContent {
    uri: string;
    url?: string;
    canonicalUri?: string;
    variantId?: string | null;
}
declare const setActivePreviewContent: (content: ActivePreviewContent | null) => void;
declare const getActivePreviewContent: (currentPreviewUrl?: string) => ActivePreviewContent | null;
export { getActivePreviewContent, setActivePreviewContent };
