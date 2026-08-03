package com.condation.cms.extensions;

/*-
 * #%L
 * CMS Extensions
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ui.elements.ContentTypes;
import com.condation.cms.api.ui.elements.fields.CheckboxField;
import com.condation.cms.api.ui.elements.fields.CodeField;
import com.condation.cms.api.ui.elements.fields.ColorField;
import com.condation.cms.api.ui.elements.fields.DateField;
import com.condation.cms.api.ui.elements.fields.DateTimeField;
import com.condation.cms.api.ui.elements.fields.DividerField;
import com.condation.cms.api.ui.elements.fields.EasyMdeField;
import com.condation.cms.api.ui.elements.fields.EmailField;
import com.condation.cms.api.ui.elements.fields.ListField;
import com.condation.cms.api.ui.elements.fields.MarkdownField;
import com.condation.cms.api.ui.elements.fields.MediaField;
import com.condation.cms.api.ui.elements.fields.NumberField;
import com.condation.cms.api.ui.elements.fields.RadioField;
import com.condation.cms.api.ui.elements.fields.RangeField;
import com.condation.cms.api.ui.elements.fields.ReferenceField;
import com.condation.cms.api.ui.elements.fields.SelectField;
import com.condation.cms.api.ui.elements.fields.StringField;
import com.condation.cms.api.ui.elements.fields.TagsField;
import com.condation.cms.api.ui.elements.fields.TextAreaField;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypesJavaScriptInteropTest {

	@Test
	void acceptsExistingJavaScriptObjectLiteralRegistrations() {
		ContentTypes contentTypes = new ContentTypes();

		try (Context context = Context.newBuilder("js")
				.allowHostAccess(HostAccess.ALL)
				.build()) {
			context.getBindings("js").putMember("contentTypes", contentTypes);
			context.eval("js", """
					contentTypes.registerPageTemplate({
					  name: 'StartPage',
					  template: 'start.html',
					  contentFolder: 'content',
					  forms: {
					    settings: {
					      fields: [{ type: 'text', name: 'title', required: true }]
					    }
					  }
					});
					contentTypes.registerSectionEntryTemplate({
					  section: 'main', name: 'Hero', template: 'hero.html'
					});
					contentTypes.registerListItemType({
					  name: 'features', form: { fields: [{ type: 'text', name: 'title' }] }
					});
					""");
		}

		var pageTemplate = contentTypes.getPageTemplate("StartPage").orElseThrow();
		assertThat(pageTemplate.contentFolder()).isEqualTo("content");
		assertThat(pageTemplate.getForm("settings").fields())
				.singleElement()
				.isInstanceOfSatisfying(StringField.class, field -> {
					assertThat(field.getName()).isEqualTo("title");
					assertThat(field.isRequired()).isTrue();
				});
		assertThat(contentTypes.getSectionEntryTemplates("main")).hasSize(1);
		assertThat(contentTypes.getListItemTypes()).hasSize(1);
	}

	@Test
	void convertsEverySupportedJavaScriptFieldIntoItsJavaClass() {
		ContentTypes contentTypes = new ContentTypes();

		try (Context context = Context.newBuilder("js")
				.allowHostAccess(HostAccess.ALL)
				.build()) {
			context.getBindings("js").putMember("contentTypes", contentTypes);
			context.eval("js", """
					contentTypes.registerPageTemplate({
					  name: 'AllFields',
					  template: 'all.html',
					  forms: { settings: { fields: [
					    { type: 'text', name: 'text', title: 'Text' },
					    { type: 'textarea', name: 'textarea', title: 'Textarea', rows: 8 },
					    { type: 'email', name: 'email', title: 'Email' },
					    { type: 'code', name: 'code', title: 'Code', height: '400px' },
					    { type: 'markdown', name: 'markdown', title: 'Markdown', height: '400px' },
					    { type: 'easymde', name: 'easymde', title: 'EasyMDE' },
					    { type: 'number', name: 'number', title: 'Number', options: { min: 1, max: 5, step: 1 } },
					    { type: 'date', name: 'date', title: 'Date' },
					    { type: 'datetime', name: 'datetime', title: 'DateTime' },
					    { type: 'color', name: 'color', title: 'Color' },
					    { type: 'range', name: 'range', title: 'Range', options: { min: 0, max: 100, step: 5 } },
					    { type: 'select', name: 'select', title: 'Select', options: { choices: ['one', { label: 'Two', value: 'two' }] } },
					    { type: 'radio', name: 'radio', title: 'Radio', options: { choices: [{ label: 'One', value: 'one' }] } },
					    { type: 'checkbox', name: 'checkbox', title: 'Checkbox', options: { choices: [{ label: 'One', value: 'one' }] } },
					    { type: 'divider', title: 'Divider' },
					    { type: 'media', name: 'media', title: 'Media' },
					    { type: 'list', name: 'list', title: 'List', options: { nameField: 'title' } },
					    { type: 'reference', name: 'reference', title: 'Reference', options: { siteid: 'other' } },
					    { type: 'tags', name: 'tags', title: 'Tags', options: { taxonomy: 'topics' } }
					  ] } }
					});
					""");
		}

		assertThat(contentTypes.getPageTemplate("AllFields").orElseThrow()
				.getForm("settings").fields())
				.extracting(Object::getClass)
				.containsExactly(
						StringField.class, TextAreaField.class, EmailField.class, CodeField.class,
						MarkdownField.class, EasyMdeField.class, NumberField.class, DateField.class,
						DateTimeField.class, ColorField.class, RangeField.class, SelectField.class,
						RadioField.class, CheckboxField.class, DividerField.class, MediaField.class,
						ListField.class, ReferenceField.class, TagsField.class);
	}
}
