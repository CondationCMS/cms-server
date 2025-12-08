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
import { openModal } from '@cms/modules/modal.js';
import { i18n } from '@cms/modules/localization.js';

const alertSelect = (options) => {
    return new Promise((resolve) => {
        const modalBody = `
            <select id="modalSelect" class="form-select">
                <option value="">${options.placeholder || i18n.t("alerts.select.placeholder", "Select a element")}</option>
                ${Object.entries(options.values || {}).map(([key, value]) => `<option value="${key}">${value}</option>`).join('')}
            </select>`;

        openModal({
            title: options.title || i18n.t("alerts.select.title", "Select element"),
            body: modalBody,
            onOk: () => {
                const selectedValue = document.getElementById('modalSelect').value;
                resolve(selectedValue);
            },
            onCancel: () => resolve(null)
        });
    });
};

const alertError = (options) => {
    openModal({
        title: options.title || i18n.t("alerts.error.title", "Error"),
        body: `<p>${options.message || i18n.t("alerts.error.message", "Some error occured")}</p>`,
    });
};


const alertConfirm = (options) => {
    return new Promise((resolve) => {
        openModal({
            title: options.title || i18n.t("alerts.confirm.title", "Are you sure?"),
            body: `<p>${options.message || i18n.t("alerts.confirm.message", "You won't be able to revert this!")}</p>`,
            onOk: () => resolve(true),
            onCancel: () => resolve(false)
        });
    });
};

const alertPrompt = (options) => {
    return new Promise((resolve) => {
        const modalBody = `
            <label for="modalInput" class="form-label">${options.label || i18n.t("alerts.prompt.label", "Input")}</label>
            <input type="text" id="modalInput" class="form-control" placeholder="${options.placeholder || i18n.t("alerts.prompt.placeholder", "Enter your input")}">`;

        openModal({
            title: options.title || i18n.t("alerts.prompt.title", "Enter value?"),
            body: modalBody,
            validate: () => {
                const value = document.getElementById('modalInput').value;
                if (options.validator) {
                    const validationResult = options.validator(value);
                    return validationResult === null || validationResult === undefined;
                }
                return true;
            },
            onOk: () => {
                const value = document.getElementById('modalInput').value;
                resolve(value);
            },
            onCancel: () => resolve(null)
        });
    });
};

export { alertSelect, alertError, alertConfirm, alertPrompt };