import { $hooks } from 'system/hooks.mjs';
import { $templates } from 'system/templates.mjs';


$hooks.registerAction("system/content/tags", (context) => {
	context.arguments().get("tags").put(
			"theme_name",
			(params) => `Hello, I'm your <b>demo</b> theme.`
	)
	return null;
})

$hooks.registerAction("system/template/function", (context) => {
	context.arguments().get("functions").put(
			"fn_message",
			(params) => `<div style="color: ${params.color}">${params.message}</div>`
	)
	return null;
})

$hooks.registerAction("system/template/component", (context) => {
	context.arguments().get("components").put(
			"component",
			(params) => `<div style="color: ${params.color}">${params.message}</div>`
	)
	
	context.arguments().get("components").put(
			"tempcomp",
			(params) => {
				var model = {
					"name": params.get("name"),
					"message": params.get("message")
				}
				return $templates.render("components/test.html", model);
			}
	)
	
	return null;
})

$hooks.registerFilter("module/ui/translations", (context) => {
	var translations = context.value()
	
	translations.en["field.title"] = "Title";
	translations.de["field.title"] = "Titel";
	
	translations.en["field.parent.text"] = "Parent-Text";
	translations.de["field.parent.text"] = "Eltern-Text";
	
	translations.en["field.description"] = "Description";
	translations.de["field.description"] = "Beschreibung";
	
	return translations;
})