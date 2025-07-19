import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("system/content/tags", (context) => {
	context.arguments().get("tags").put(
			"parent_name",
			(params) => `Hello, I'm your father.`
	)
	return null;
})