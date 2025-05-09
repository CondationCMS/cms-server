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
document.addEventListener("DOMContentLoaded", function () {


	/////// Prevent closing from click inside dropdown
	document.querySelectorAll('.dropdown-menu').forEach(function (element) {
		element.addEventListener('click', function (e) {
			e.stopPropagation();
		});
	})



	// make it as accordion for smaller screens
	if (window.innerWidth < 992) {

		// close all inner dropdowns when parent is closed
		document.querySelectorAll('.navbar .dropdown').forEach(function (everydropdown) {
			everydropdown.addEventListener('hidden.bs.dropdown', function () {
				// after dropdown is hidden, then find all submenus
				this.querySelectorAll('.submenu').forEach(function (everysubmenu) {
					// hide every submenu as well
					everysubmenu.style.display = 'none';
				});
			})
		});

		document.querySelectorAll('.dropdown-menu a').forEach(function (element) {
			element.addEventListener('click', function (e) {

				let nextEl = this.nextElementSibling;
				if (nextEl && nextEl.classList.contains('submenu')) {
					// prevent opening link if link needs to open dropdown
					e.preventDefault();
					console.log(nextEl);
					if (nextEl.style.display == 'block') {
						nextEl.style.display = 'none';
					} else {
						nextEl.style.display = 'block';
					}

				}
			});
		})
	}
	// end if innerWidth

});
// DOMContentLoaded  end
