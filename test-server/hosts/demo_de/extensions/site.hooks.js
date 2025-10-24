import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("theme/template/header", (context) => {
	return "<!-- header 1 -->";
})
$hooks.registerAction("theme/template/header", (context) => {
	return "<!-- header 2 -->";
})