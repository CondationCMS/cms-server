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
import {openSidebar} from '/manager/js/sidebar.js'
import {createForm} from '/manager/js/forms.js'
		// hook.js
export function runAction(params) {

	const form = createForm({
		fields: [
			{type: 'text', name: 'username', title: 'Benutzername'},
			{type: 'email', name: 'email', title: 'E-Mail'},
			{
				type: 'select',
				name: 'gender',
				title: 'Geschlecht',
				options: [
					{label: 'Bitte wählen', value: ''},
					{label: 'Männlich', value: 'm'},
					{label: 'Weiblich', value: 'w'},
					{label: 'Divers', value: 'd'}
				]
			}
		],
		values: {
			username: 'Max',
			email: 'max@beispiel.de',
			gender: 'm'
		}
	});



	openSidebar({
		title: 'Example Model',
		body: 'modal body',
		form: form,
		onCancel: (event) => console.log("modal canceled"),
		onOk: (event) => console.log("modal ok", form.getData()),
	});
}
