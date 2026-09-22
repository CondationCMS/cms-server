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
export interface AlternateDto {
    site: string;
    locale: string;
    url?: string;
    managerDeepLink?: string;
}
declare const getAlternates: (options: {
    uri: string;
}) => Promise<{
    alternates: AlternateDto[];
}>;
declare const addAlternate: (options: {
    uri: string;
    targetSite: string;
    alternateUri: string;
}) => Promise<{
    uri: string;
}>;
declare const removeAlternate: (options: {
    uri: string;
    targetSite: string;
}) => Promise<{
    uri: string;
}>;
export { getAlternates, addAlternate, removeAlternate };
