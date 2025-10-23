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
import { EventBus } from './modules/event-bus.js';
import { i18n, localizeUi } from './modules/localization.js';
window.addEventListener("DOMContentLoaded", async () => {
    /*
    await i18n.init()
    await localizeUi();
    var langSelect = document.querySelector(`[data-cms-i18n-lang='${i18n.getLocale()}']`)
    if (langSelect) {
        langSelect.classList.add("active");
    }
    */
    document.querySelectorAll(".cms-lang-selector").forEach($elem => {
        $elem.addEventListener("click", async () => {
            i18n.setLocale($elem.getAttribute("data-cms-i18n-lang"));
            await localizeUi();
            document.querySelectorAll(".cms-lang-selector").forEach(el => {
                el.classList.remove("active");
            });
            $elem.classList.add("active");
            const dropdown = bootstrap.Dropdown.getInstance($elem.closest('.dropdown'))
                || new bootstrap.Dropdown($elem.closest('.dropdown').querySelector('[data-bs-toggle="dropdown"]'));
            dropdown.hide();
        });
    });
    EventBus.on("ui:localeChanged", (data) => {
    });
    const actionElements = document.querySelectorAll('[data-cms-action-definition]');
    actionElements.forEach(element => {
        try {
            element.addEventListener("click", (action) => {
                const definition = element.getAttribute('data-cms-action-definition');
                try {
                    const action = JSON.parse(definition);
                    if (action.type === "hook") {
                        executeHookAction(action);
                    }
                    else if (action.type === "script") {
                        executeScriptAction(action);
                    }
                }
                catch (e) {
                    console.error('error parsing error definition', e);
                }
            });
        }
        catch (e) {
            console.error('', e);
        }
    });
});
