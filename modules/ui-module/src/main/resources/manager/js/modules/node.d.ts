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
/**
 * Retrieves a nested value from an object using a dot-notated path like "meta.title"
 * @param {object} sourceObj - The object to retrieve the value from
 * @param {string} path - Dot-notated string path, e.g., "meta.title"
 * @returns {*} - The value found at the given path, or undefined if not found
 */
export function getValueByPath(sourceObj: object, path: string): any;
/**
 * Builds a values object from an array of form fields
 * @param {Array} fields - Array of form field objects, each with a .name property
 * @param {object} sourceObj - The source object to extract the values from
 * @returns {object} values - An object mapping field names to their corresponding values
 */
export function buildValuesFromFields(fields: any[], sourceObj: object): object;
