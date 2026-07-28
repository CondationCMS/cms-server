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
export interface VariantDto {
    id: string;
    uri: string;
    url: string;
    meta: Record<string, unknown>;
}
export interface GetVariantsOptions {
    uri: string;
    siteId?: string;
}
export interface GetVariantsResult {
    uri: string;
    variants: VariantDto[];
}
declare const getVariants: (options: GetVariantsOptions) => Promise<GetVariantsResult>;
export { getVariants };
