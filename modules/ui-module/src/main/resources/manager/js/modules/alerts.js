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

import sweetalert2 from 'https://cdn.jsdelivr.net/npm/sweetalert2@11.21.2/+esm'

const alertSelect = async (options) => {

	console.log("alertSelect received:", options);
	console.log("inputOptions to be used:", options.values);
	/*
		const { value: selected } = await sweetalert2.fire({
			title: "Please select",
			input: "select",
			//backdrop: false,
			//allowOutsideClick: false,
			//allowEscapeKey: false,
			inputOptions: options.values,
			inputPlaceholder: "Select",
			showCancelButton: true,
			//inputValidator: options.validator || null
		});
	
		const { value: selectedValue } = await sweetalert2.fire({
			title: options.title || "Select element",
			input: "select",
			inputOptions: options.values,
			inputPlaceholder: options.placeholder || "Select a element",
			showCancelButton: true,
			inputValidator: options.validator || null
		});
	*/
	const { value: selectedValue } = await sweetalert2.fire({
		title: options.title || "Select element",
		input: "select",
		inputOptions: options.values || {},
		inputPlaceholder: options.placeholder || "Select a element",
		showCancelButton: true
	});

	return selectedValue
};

const alertError = async () => {
	sweetalert2.fire({
  		icon: "error",
  		title: options.title || "Error title",
  		text: options.message || "Some error occured",
	});
}



export { alertSelect, alertError }