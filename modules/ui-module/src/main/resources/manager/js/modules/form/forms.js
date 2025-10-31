/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import { createID } from "./utils.js";
import { TextField } from "./field.text.js";
import { MailField } from "./field.mail.js";
import { CodeField } from "./field.code.js";
import { SelectField } from "./field.select.js";
import { MarkdownField } from "./field.markdown.js";
import { EasyMDEField } from "./field.easymde.js";
import { NumberField } from "./field.number.js";
import { DateField } from "./field.date.js";
import { ColorField } from "./field.color.js";
import { DateTimeField } from "./field.datetime.js";
import { RangeField } from "./field.range.js";
import { RadioField } from "./field.radio.js";
import { CheckboxField } from "./field.checkbox.js";
import { Divider } from "./field.divider.js";
import { MediaField } from "./field.media.js";
import { ListField } from "./field.list.js";
import { TextAreaField } from "./field.textarea.js";
import { ReferenceField } from "./field.reference.js";
const createForm = (options) => {
    const fields = options.fields || [];
    const values = options.values || {};
    const formId = createID();
    const context = {
        formElement: null,
        fields: fields
    };
    const fieldHtml = fields.map(field => {
        const val = values[field.name] || '';
        switch (field.type) {
            case 'email':
                return MailField.markup(field, val);
            case 'text':
                return TextField.markup(field, val);
            case 'select':
                return SelectField.markup(field, val);
            case 'code':
                return CodeField.markup(field, val);
            case 'markdown':
                return MarkdownField.markup(field, val);
            case 'easymde':
                return EasyMDEField.markup(field, val);
            case 'number':
                return NumberField.markup(field, val);
            case 'date':
                return DateField.markup(field, val);
            case 'datetime':
                return DateTimeField.markup(field, val);
            case 'color':
                return ColorField.markup(field, val);
            case 'range':
                return RangeField.markup(field, val);
            case 'radio':
                return RadioField.markup(field, val);
            case 'checkbox':
                return CheckboxField.markup(field, val);
            case 'divider':
                return Divider.markup(field, val);
            case 'media':
                return MediaField.markup(field, val);
            case 'list':
                return ListField.markup(field, val);
            case 'textarea':
                return TextAreaField.markup(field, val);
            case 'reference':
                return ReferenceField.markup(field, val);
            default:
                return '';
        }
    }).join('\n');
    const html = `
		<form id="${formId}" class="needs-validation h-100 cms-form" novalidate>
			${fieldHtml}
		</form>
	`;
    const init = (container) => {
        if (typeof container === 'string') {
            container = document.querySelector(container);
        }
        if (!container) {
            console.error("From container not found.");
            return;
        }
        container.innerHTML = html;
        context.formElement = container.querySelector('form');
        context.formElement.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.tagName.toLowerCase() !== 'textarea') {
                e.preventDefault();
            }
        });
        context.formElement.addEventListener('submit', (e) => {
            e.preventDefault();
            e.stopPropagation();
            context.formElement.classList.add('was-validated');
        });
        CodeField.init(context);
        MarkdownField.init(context);
        EasyMDEField.init(context);
        MediaField.init(context);
        ListField.init(context);
        ReferenceField.init(context);
    };
    const getData = () => {
        if (!context.formElement) {
            console.warn("Form not initialised.");
            return {};
        }
        const data = {
            ...TextField.data(context),
            ...SelectField.data(context),
            ...MailField.data(context),
            ...CodeField.data(context),
            ...MarkdownField.data(context),
            ...EasyMDEField.data(context),
            ...NumberField.data(context),
            ...DateField.data(context),
            ...DateTimeField.data(context),
            ...ColorField.data(context),
            ...RangeField.data(context),
            ...RadioField.data(context),
            ...CheckboxField.data(context),
            ...MediaField.data(context),
            ...ListField.data(context),
            ...TextAreaField.data(context),
            ...ReferenceField.data(context)
        };
        return data;
    };
    return {
        init,
        getData,
        getRawData: () => {
            let data = getData();
            return flattenFormData(data);
        }
    };
};
const flattenFormData = (input) => {
    const result = {};
    for (const key in input) {
        const value = input[key].value;
        const parts = key.split(".");
        let current = result;
        for (let i = 0; i < parts.length; i++) {
            const part = parts[i];
            if (i === parts.length - 1) {
                current[part] = value;
            }
            else {
                if (!(part in current))
                    current[part] = {};
                current = current[part];
            }
        }
    }
    return result;
};
export { createForm };
;
