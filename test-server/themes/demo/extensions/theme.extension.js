import { $hooks } from 'system/hooks.mjs';
import { $templates } from 'system/templates.mjs';


$hooks.registerAction("system/content/shortCodes", ({shortCodes}) => {
	shortCodes.put(
			"theme_name",
			(params) => `Hello, I'm your <b>demo</b> theme.`
	)
	return null;
})

$hooks.registerAction("system/content/shortCodes", ({shortCodes}) => {
	shortCodes.put(
			"say_hello",
			({name}) => `Hello, ${name}`
	)
	return null;
})

$hooks.registerAction("system/template/function", ({functions}) => {
	functions.put(
			"fn_message",
			({color, message}) => `<div style="color: ${color}">MESSAGE: ${message}</div>`
	)
	return null;
})

$hooks.registerAction("system/template/component", ({components}) => {
	components.put(
			"component",
			({color, message}) => `<div style="color: ${color}">COMPONENT: ${message}</div>`
	)
	
	components.put(
			"tempcomp",
			(params) => {
				var model = {
					"name": params.name,
					"message_text": params.message
				}
				return "rendered: " + $templates.render("components/test.html", model);
			}
	)
	
	return null;
})

$hooks.registerFilter("module/ui/translations", ({translations}) => {
	
	translations.en["field.title"] = "Title";
	translations.de["field.title"] = "Titel";
	
	translations.en["field.parent.text"] = "Parent-Text";
	translations.de["field.parent.text"] = "Eltern-Text";
	
	translations.en["field.description"] = "Description";
	translations.de["field.description"] = "Beschreibung";
	
	return translations;
})

$hooks.registerAction("system/layout/html/header", (args) => {
	return "<!-- this comes into the header -->";
})
$hooks.registerAction("system/layout/html/footer", (args) => {
	return "<!-- this comes into the footer -->";
})