import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("system/content/shortcodes", (context) => {
	context.arguments().get("shortCodes").put(
			"theme_name",
			(params) => `Hello, I'm your <b>demo</b> theme.`
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