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
const createID = () => "id" + Math.random().toString(16).slice(2);
const utcToLocalDateTimeInputValue = (utcString) => {
    const date = new Date(utcString);
    if (isNaN(date.getTime()))
        return "";
    const pad = (n) => String(n).padStart(2, '0');
    const yyyy = date.getFullYear();
    const MM = pad(date.getMonth() + 1);
    const dd = pad(date.getDate());
    const hh = pad(date.getHours());
    const mm = pad(date.getMinutes());
    return `${yyyy}-${MM}-${dd}T${hh}:${mm}`;
};
function getUTCDateTimeFromInput(inputElement) {
    const localValue = inputElement.value; // "2025-05-31T03:00"
    if (!localValue)
        return null;
    const localDate = new Date(localValue); // interpretiert als lokale Zeit
    return localDate.toISOString(); // → "2025-05-31T01:00:00.000Z"
}
function utcToLocalDateInputValue(utcString) {
    const date = new Date(utcString);
    if (isNaN(date.getTime()))
        return "";
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
function getUTCDateFromInput(inputElement) {
    const dateOnly = inputElement.value; // z. B. "2025-05-31"
    if (!dateOnly)
        return null;
    const date = new Date(dateOnly); // wird zu 00:00 lokale Zeit
    return date.toISOString(); // → "2025-05-30T22:00:00.000Z" (wenn in CEST)
}
export { createID, utcToLocalDateTimeInputValue, getUTCDateTimeFromInput, utcToLocalDateInputValue, getUTCDateFromInput };
