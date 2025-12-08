import { createID } from "@cms/modules/form/utils.js";
const createCheckboxField = (options, value = []) => {
    const id = createID();
    const key = options.key || "";
    const name = options.name || id;
    const title = options.title || "";
    const choices = options.options.choices || [];
    const selectedValues = new Set(value);
    const checkboxes = choices.map((choice, idx) => {
        const inputId = `${id}-${idx}`;
        const checked = selectedValues.has(choice.value) ? 'checked' : '';
        return `
			<div class="form-check cms-form-field">
				<input class="form-check-input" type="checkbox" name="${name}" id="${inputId}" value="${choice.value}" ${checked}>
				<label class="form-check-label" for="${inputId}">
					${choice.label}
				</label>
			</div>
		`;
    }).join('');
    return `
		<div class="mb-3" data-cms-form-field-type="checkbox">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			${checkboxes}
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='checkbox']").forEach(container => {
        const name = container.querySelector("input[type='checkbox']").name;
        const checkedBoxes = container.querySelectorAll("input[type='checkbox']:checked");
        const values = Array.from(checkedBoxes).map((el) => el.value);
        data[name] = {
            type: 'checkbox',
            value: values
        };
    });
    return data;
};
export const CheckboxField = {
    markup: createCheckboxField,
    init: (context) => { },
    data: getData
};
