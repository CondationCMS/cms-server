import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("manager/contentTypes/register", (context) => {
	context.arguments().get("contentTypes").put(
			"component",
			(params) => `<div style="color: ${params.color}">${params.message}</div>`
	)
	return null;
})