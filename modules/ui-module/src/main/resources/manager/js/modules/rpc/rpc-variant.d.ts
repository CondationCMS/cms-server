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
    canonical: {
        uri: string;
        url: string;
        title: string;
        template: string;
    };
    activeVariantId?: string | null;
    variants: VariantDto[];
}
export interface CreateVariantOptions {
    uri: string;
    id: string;
    title: string;
    template: string;
    copyContent: boolean;
}
export interface CreateVariantResult {
    id: string;
    uri: string;
    url: string;
}
export interface DeleteVariantResult {
    id: string;
    url: string;
}
export interface VariantSelectorDto {
    id: string;
    label: string;
}
export interface GetVariantSelectorsResult {
    selector: string;
    selectors: VariantSelectorDto[];
}
declare const getVariants: (options: GetVariantsOptions) => Promise<GetVariantsResult>;
declare const createVariant: (options: CreateVariantOptions) => Promise<CreateVariantResult>;
declare const deleteVariant: (uri: string, id: string) => Promise<DeleteVariantResult>;
declare const getVariantSelectors: (uri: string) => Promise<GetVariantSelectorsResult>;
declare const setVariantSelector: (uri: string, selector: string) => Promise<{
    selector: string;
}>;
export { createVariant, deleteVariant, getVariants, getVariantSelectors, setVariantSelector };
