import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("system/template/supplier", (context) => {
	context.arguments().get("suppliers").add(
			"myName",
			() => "My name is CondationCMS"
	)
	return null;
})

$hooks.registerAction("system/template/function", (context) => {
	context.arguments().get("functions").add(
			"getHello",
		(name) => "Hello " + name + "!!"
	)
	return null;
})
