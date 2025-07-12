import { $hooks } from 'system/hooks.mjs';

$hooks.registerFilter("manager/contentTypes/register", (context) => {
	var contentTypes = context.value();
	contentTypes.registerPageTemplate({
		name: "StartPage",
		template: "start.html"
	});
	contentTypes.registerPageTemplate({
		name: "Default",
		template: "default.html"
	});
	contentTypes.registerSectionTemplate({
		section: "asection",
		name: "SectionTemplate",
		template: "section.html"
	});
	return contentTypes;
})