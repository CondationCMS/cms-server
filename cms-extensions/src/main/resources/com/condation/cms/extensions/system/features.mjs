export const AuthFeature = Java.type("com.condation.cms.api.feature.features.AuthFeature").class
export const ConfigurationFeature = Java.type("com.condation.cms.api.feature.features.ConfigurationFeature").class
export const ContentNodeMapperFeature = Java.type("com.condation.cms.api.feature.features.ContentNodeMapperFeature").class
export const ContentParserFeature = Java.type("com.condation.cms.api.feature.features.ContentParserFeature").class
export const ContentRenderFeature = Java.type("com.condation.cms.api.feature.features.ContentRenderFeature").class
export const CurrentNodeFeature = Java.type("com.condation.cms.api.feature.features.CurrentNodeFeature").class
export const CurrentTaxonomyFeature = Java.type("com.condation.cms.api.feature.features.CurrentTaxonomyFeature").class
export const DBFeature = Java.type("com.condation.cms.api.feature.features.DBFeature").class
export const EventBusFeature = Java.type("com.condation.cms.api.feature.features.EventBusFeature").class
export const HookSystemFeature = Java.type("com.condation.cms.api.feature.features.HookSystemFeature").class
export const InjectorFeature = Java.type("com.condation.cms.api.feature.features.InjectorFeature").class
export const IsDevModeFeature = Java.type("com.condation.cms.api.feature.features.IsDevModeFeature").class
export const IsPreviewFeature = Java.type("com.condation.cms.api.feature.features.IsPreviewFeature").class
export const MarkdownRendererFeature = Java.type("com.condation.cms.api.feature.features.MarkdownRendererFeature").class
export const ModuleManagerFeature = Java.type("com.condation.cms.api.feature.features.ModuleManagerFeature").class
export const RequestFeature = Java.type("com.condation.cms.api.feature.features.RequestFeature").class
export const ServerPropertiesFeature = Java.type("com.condation.cms.api.feature.features.ServerPropertiesFeature").class
export const SiteMediaServiceFeature = Java.type("com.condation.cms.api.feature.features.SiteMediaServiceFeature").class
export const SitePropertiesFeature = Java.type("com.condation.cms.api.feature.features.SitePropertiesFeature").class
export const ThemeFeature = Java.type("com.condation.cms.api.feature.features.ThemeFeature").class

export const $features = {
	get : (feature) => {
		return requestContext.get(feature)
	},
	has : (feature) => {
		return requestContext.has(feature)
	}
}