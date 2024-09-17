import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("system/content/shortcodes", (context) => {
	context.arguments().get("shortCodes").put(
			"theme_name",
			(params) => `Hello, I'm your <b>default</b> theme.`
	)
	return null;
})
