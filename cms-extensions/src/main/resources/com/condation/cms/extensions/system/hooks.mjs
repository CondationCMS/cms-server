import { HookSystemFeature, $features } from 'system/features.mjs';


const hooks = $features.get(HookSystemFeature).hookSystem()

export const $hooks = {
	registerAction : (name, fun, priority) => {
		// wrap: pass arguments as plain JS object so callers can destructure
		const wrapped = (context) => {
			const args = {};
			for (const key of context.arguments().keySet()) {
				args[key] = context.arguments().get(key);
			}
			return fun(args);
		};
		if (priority) {
			hooks.registerAction(name, wrapped, priority)
		} else {
			hooks.registerAction(name, wrapped)
		}
	},
	registerFilter : (name, fun, priority) => {
		// wrap: pass value directly instead of FilterContext
		const wrapped = (context) => fun(context.value());
		if (priority) {
			hooks.registerFilter(name, wrapped, priority)
		} else {
			hooks.registerFilter(name, wrapped)
		}
	}
}
