import { $hooks } from 'system/hooks.mjs';
import { $shortcodes } from 'system/shortcodes.mjs';


$hooks.registerAction("system/content/shortcodes", (context) => {
	context.arguments().get("shortCodes").put(
			"hello",
			(params) => `Hello ${params.get("name")}, I'm a TAG!`
	)
	return null;
})

$hooks.registerAction("system/content/shortcodes", (context) => {
	context.arguments().get("shortCodes").put(
			"name_age",
			(params) => `Hello ${params.get("name")}, your age is ${params.get("age")}!`
	)
	return null;
})

