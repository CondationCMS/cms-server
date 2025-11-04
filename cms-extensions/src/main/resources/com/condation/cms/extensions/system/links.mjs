import { IsPreviewFeature, SitePropertiesFeature, $features } from 'system/features.mjs';


const siteProperties = $features.get(SitePropertiesFeature).siteProperties()

export const $links = {
	createUrl : (url) => {
				// Falls absolute URL (http/https), direkt zurückgeben
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return url;
		}

		// Context Path holen (z. B. aus globaler Variable oder Config)
		const contextPath = siteProperties.contextPath();

		// Wenn ContextPath nicht "/", dann davor setzen
		if (contextPath !== "/") {
			url = contextPath + url;
		}

		// Preview-Parameter anhängen, falls aktiv
		if ($features.has(IsPreviewFeature)) {
			const feature = $features.get(IsPreviewFeature);
			const previewValue = feature.mode().getValue();

			// Prüfen, ob bereits Parameter vorhanden sind
			const separator = url.includes("?") ? "&" : "?";
			url = `${url}${separator}preview=${previewValue}`;
		}

		return url;
	}
}