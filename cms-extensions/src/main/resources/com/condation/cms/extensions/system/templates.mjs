import { TemplateEngineFeature, $features } from 'system/features.mjs';


const templateEngine = $features.get(TemplateEngineFeature)

export const $templates = {
	render : (template, model) => {
		return templateEngine.render(template, model, requestContext);
	}
}