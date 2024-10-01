import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("system/content/shortcodes", (context) => {
	context.arguments().get("shortCodes").put(
			"parent_name",
			(params) => `Hello, I'm your father.`
	)
	return null;
})