import { HookSystemFeature, $features } from 'system/features.mjs';


const hooks = $features.get(HookSystemFeature).hookSystem()

export const $hooks = {
	registerAction : (name, fun, priority) => {
		if (priority) {
			hooks.registerAction(name, fun, priority)
		} else {
			hooks.registerAction(name, fun)
		}
	},
	registerFilter : (name, fun, priority) => {
		if (priority) {
			hooks.registerFilter(name, fun, priority)
		} else {
			hooks.registerFilter(name, fun)
		}
	}
}