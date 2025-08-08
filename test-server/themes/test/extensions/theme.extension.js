import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("system/content/tags", (context) => {
	context.arguments().get("tags").put(
			"theme_name",
			(params) => `Hello, I'm your <b>test</b> theme.`
	)
	return null;
})