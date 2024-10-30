import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("system/db/query/operations", (context) => {
	context.arguments().get("operations").add(
			"none",
			(fieldValue, value) => {
				return false
			}
	)
	return null;
})