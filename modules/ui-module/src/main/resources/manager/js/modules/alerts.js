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

import Swal from '../libs/sweetalert2.min.js'
import { i18n } from './localization.js'

const sweetalert2 = Swal.mixin({
  customClass: {
    confirmButton: "btn btn-success",
    cancelButton: "btn btn-danger"
  },
  buttonsStyling: false
});

const alertSelect = async (options) => {
	const { value: selectedValue } = await sweetalert2.fire({
		title: options.title || i18n.t("alerts.select.title", "Select element"),
		input: "select",
		inputOptions: options.values || {},
		inputPlaceholder: options.placeholder || i18n.t("alerts.select.placeholder", "Select a element"),
		showCancelButton: true
	});

	return selectedValue
};

const alertError = async () => {
	sweetalert2.fire({
  		icon: "error",
  		title: options.title || i18n.t("alerts.error.title", "Error"),
  		text: options.message || i18n.t("alerts.error.message", "Some error occured"),
	});
}

const alertConfirm = async (options) => {
	const { isConfirmed } = await sweetalert2.fire({
		title: options.title || i18n.t("alerts.confirm.title", "Are you sure?"),
		text: options.message || i18n.t("alerts.confirm.message", "You won't be able to revert this!"),
		icon: "warning",
		showCancelButton: true,
		confirmButtonText: options.confirmText || i18n.t("alerts.confirm.button.ok", "Yes, delete it!"),
		cancelButtonText: options.cancelText || i18n.t("alerts.confirm.button.cancel", "No, cancel!"),
	});

	return isConfirmed
};

const alertPrompt = async (options) => {
	const { value } = await sweetalert2.fire({
		title: options.title || i18n.t("alerts.prompt.title", "Enter value?"),
		input: 'text',
		inputLabel: options.label || i18n.t("alerts.prompt.label", "Input"),
		inputPlaceholder: options.placeholder || i18n.t("alerts.prompt.placeholder", "Enter your input"),
		showCancelButton: true,
		inputValidator: options.validator || null
	});

	return value
};

export { alertSelect, alertError, alertConfirm, alertPrompt }