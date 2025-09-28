import { $hooks } from 'system/hooks.mjs';

$hooks.registerFilter("manager/media/forms", (context) => {
	var mediaForms = context.value();
	mediaForms.registerForm("meta", {
		fields: [
			{
				type: "text",
				name: "alt",
				title: "Alt-Text"
			}
		]
	});
	return mediaForms;
})

$hooks.registerFilter("manager/contentTypes/register", (context) => {
	var contentTypes = context.value();
	contentTypes.registerPageTemplate({
		name: "StartPage",
		template: "start.html",
		forms: {
			settings: [
				{
					type: 'divider',
					name: 'divider',
					title: 'Custom attributes',
				},
				{
					type: "media",
					name: "media_url",
					title: "Media"
				},
				{
					type: 'color',
					name: 'background_color',
					title: 'Background Color'
				},
				{
					type: "range",
					name: "range_test",
					title: "RangField"
				},
				{
					type: "radio",
					name: "choose_color",
					title: "Farbe wählen",
					options: {
						choices: [
							{label: "Rot", value: "red"},
							{label: "Grün", value: "green"},
							{label: "Blau", value: "blue"}
						]
					}
				},
				{
					name: "features",
					title: "Funktionen auswählen",
					type: "checkbox",
					options: {
						choices: [
							{label: "Suche", value: "search"},
							{label: "Filter", value: "filter"},
							{label: "Export", value: "export"}
						]
					}
				},
				{
					name: "object.values",
					title: "Objekt-Liste",
					type: "list"
				}
			]
		}
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