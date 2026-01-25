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

const TextField = (overrides = {}) => ({
	type: "text",
	...overrides
});

const TitleField = TextField({
	name: "title",
	title: "Title",
});
const DescriptionField = TextField({
	name: "description",
	title: "Description",
});
const PublishDateField = {
	type: "datetime",
	name: "publish_date",
	title: "Publish Date",
};
const UnPublishDateField = {
	type: "datetime",
	name: "unpublish_date",
	title: "Unpublish Date",
};


$hooks.registerFilter("manager/contentTypes/register", (context) => {
	var contentTypes = context.value();
	contentTypes.registerPageTemplate({
		name: "StartPage",
		template: "start.html",
		forms: {
			settings: {
				fields:

					[
						{
							type: 'divider',
							name: 'divider',
							title: 'Custom attributes'
						},
						{
							type: "reference",
							name: "linked_page",
							title: "Verlinkte Seite"
						},
						{
							type: "textarea",
							name: "seo.description",
							title: "Seo Beschreibung"
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
									{ label: "Rot", value: "red" },
									{ label: "Grün", value: "green" },
									{ label: "Blau", value: "blue" }
								]
							}
						},
						{
							name: "features",
							title: "Funktionen auswählen",
							type: "checkbox",
							options: {
								choices: [
									{ label: "Suche", value: "search" },
									{ label: "Filter", value: "filter" },
									{ label: "Export", value: "export" }
								]
							}
						},
						{
							name: "object.values",
							title: "Objekt-Liste",
							type: "list"
						}
					]
			},
			// override global definition of ListItemTypes
			'object.values': {
				fields: [
					TitleField,
					DescriptionField,
					{
						name: "features",
						title: "Funktionen auswählen",
						type: "select",
						options: {
							choices: [
								{ label: "Suche", value: "search" },
								{ label: "Filter", value: "filter" },
								{ label: "Export", value: "export" }
							]
						}
					},
				]
			},
			'attributes': {
				fields: [
					{
						name: "parent.text",
						title: "Parent Text",
						type: "text"
					}
				]
			},
			'top': {
				fields: [
					{
						name: "parent.text",
						title: "Parent Text",
						type: "text"
					},
					{
						name: "description",
						title: "Description",
						type: "text"
					},
					{
						name: "count",
						type: "number",
						title: "Anzahl",
						options: {
							min: 10,
							max: 50,
							step: 1
						}
					}
				]
			}
		}
	});

	contentTypes.registerPageTemplate({
		name: "Default",
		template: "default.html"
	});
	contentTypes.registerSectionTemplate({
		section: "asection",
		name: "SectionTemplate",
		template: "section.html",
		forms: {
			attributes: {
				fields: [
					TitleField,
					DescriptionField,
					PublishDateField,
					UnPublishDateField,
					{
						type: "list",
						name: "object.values",
						title: "Object list",
						options: {
							nameField: "title"
						}
					},
					{
						type: "text",
						name: "parent.text",
						title: "Parent Text"
					},
					{
						type: "markdown",
						name: "about",
						title: "About"
					},
					{
						type: "divider",
						title: "Additional Information"
					},
					{
						type: "markdown",
						name: "about1",
						title: "About1"
					}
				]
			}
		}
	});

	/*
	global definition if ListItemTypes
	*/
	contentTypes.registerListItemType({
		name: "object.values",
		form: {
			fields: [
				{
					name: "name",
					title: "Name",
					type: "text"
				},
				{
					name: "description",
					title: "Description",
					type: "text"
				}
			]
		}
	});

	return contentTypes;
})