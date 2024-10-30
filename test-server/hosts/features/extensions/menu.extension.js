import { $hooks } from 'system/hooks.mjs';
import { NavNode } from 'system/navigation.mjs';

$hooks.registerFilter("system/navigation/top/list", (context) => {
	var nodes = context.value()
	nodes.add(2, new NavNode("Hello-Extension", "/hello-route"))
	return nodes
})