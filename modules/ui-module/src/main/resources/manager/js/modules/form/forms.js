/*-
 * #%L
 * UI Module
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
import { createID } from "@cms/modules/form/utils.js";
import { TextField } from "@cms/modules/form/field.text.js";
import { MailField } from "@cms/modules/form/field.mail.js";
import { CodeField } from "@cms/modules/form/field.code.js";
import { SelectField } from "@cms/modules/form/field.select.js";
import { MarkdownField } from "@cms/modules/form/field.markdown.js";
import { EasyMDEField } from "@cms/modules/form/field.easymde.js";
import { NumberField } from "@cms/modules/form/field.number.js";
import { DateField } from "@cms/modules/form/field.date.js";
import { ColorField } from "@cms/modules/form/field.color.js";
import { DateTimeField } from "@cms/modules/form/field.datetime.js";
import { RangeField } from "@cms/modules/form/field.range.js";
import { RadioField } from "@cms/modules/form/field.radio.js";
import { CheckboxField } from "@cms/modules/form/field.checkbox.js";
import { Divider } from "@cms/modules/form/field.divider.js";
import { MediaField } from "@cms/modules/form/field.media.js";
import { ListField } from "@cms/modules/form/field.list.js";
import { TextAreaField } from "@cms/modules/form/field.textarea.js";
import { ReferenceField } from "@cms/modules/form/field.reference.js";
import { TagsField } from "@cms/modules/form/field.tags.js";
import { i18n } from "@cms/modules/localization.js";
const createForm = (options) => {
    const fields = options.fields || [];
    const values = options.values || {};
    const formId = createID();
    const context = {
        formElement: null,
        fields: fields
    };
    const fieldHtml = fields.map((field) => {
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
            case 'tags':
                return TagsField.markup(field, val);
            default:
                return '';
        }
    }).join('\n');
    const html = `
		<form id="${formId}" class="needs-validation cms-form d-flex flex-column h-100" novalidate>
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
        if (!context.formElement) {
            console.error('Form element not found.');
            return;
        }
        context.formElement.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.tagName.toLowerCase() !== 'textarea') {
                e.preventDefault();
            }
        });
        context.formElement.addEventListener('submit', (e) => {
            e.preventDefault();
            e.stopPropagation();
            validate();
        });
        CodeField.init(context);
        MarkdownField.init(context);
        EasyMDEField.init(context);
        MediaField.init(context);
        ListField.init(context);
        ReferenceField.init(context);
        TagsField.init(context);
        markRequiredFields();
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
            ...ReferenceField.data(context),
            ...TagsField.data(context)
        };
        return data;
    };
    const getFieldContainer = (fieldName) => {
        if (!context.formElement) {
            return null;
        }
        const containers = context.formElement.querySelectorAll("[data-cms-form-field-type]");
        return Array.from(containers).find(container => {
            if (container.getAttribute('name') === fieldName) {
                return true;
            }
            return Array.from(container.querySelectorAll('[name]'))
                .some(element => element.getAttribute('name') === fieldName);
        }) || null;
    };
    const markRequiredFields = () => {
        fields.filter((field) => field.required && field.name).forEach((field) => {
            const container = getFieldContainer(field.name);
            if (!container) {
                return;
            }
            container.dataset.cmsRequired = 'true';
            container.setAttribute('aria-required', 'true');
            const label = container.querySelector('.form-label');
            if (label && !label.querySelector('.cms-required-marker')) {
                label.insertAdjacentHTML('beforeend', ' <span class="cms-required-marker text-danger" aria-hidden="true">*</span>');
            }
            const controls = Array.from(container.querySelectorAll('[name]'))
                .filter(control => control.name === field.name);
            controls.forEach(control => {
                control.setAttribute('aria-required', 'true');
                // A required attribute on every checkbox would mean that every option
                // has to be selected. Checkbox groups are validated as one field below.
                if (!(control instanceof HTMLInputElement && control.type === 'checkbox')) {
                    control.required = true;
                }
            });
            if (!container.querySelector('.invalid-feedback')) {
                const message = field.requiredMessage || i18n.t('form.validation.required', 'This field is required.');
                container.insertAdjacentHTML('beforeend', `<div class="invalid-feedback" role="alert">${message}</div>`);
            }
        });
    };
    const isEmpty = (value) => {
        if (value === null || value === undefined) {
            return true;
        }
        if (typeof value === 'string') {
            return value.trim().length === 0;
        }
        if (Array.isArray(value)) {
            return value.length === 0;
        }
        return false;
    };
    const validate = () => {
        if (!context.formElement) {
            console.warn('Form not initialised.');
            return false;
        }
        const data = getData();
        const invalidContainers = [];
        fields.filter((field) => field.required && field.name).forEach((field) => {
            const container = getFieldContainer(field.name);
            if (!container) {
                return;
            }
            const invalid = isEmpty(data[field.name]?.value);
            container.classList.toggle('is-invalid', invalid);
            container.querySelector('.invalid-feedback')?.classList.toggle('d-block', invalid);
            container.querySelectorAll('[name]').forEach(control => {
                if (control.getAttribute('name') === field.name) {
                    control.classList.toggle('is-invalid', invalid);
                    control.setAttribute('aria-invalid', String(invalid));
                }
            });
            if (invalid) {
                invalidContainers.push(container);
            }
        });
        const firstInvalidContainer = invalidContainers[0];
        if (firstInvalidContainer) {
            firstInvalidContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
            const focusTarget = firstInvalidContainer.querySelector('input:not([type="hidden"]), select, textarea, button, [tabindex]');
            focusTarget?.focus();
            return false;
        }
        return true;
    };
    return {
        init,
        validate,
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
