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
import { CollectionItemSummary } from '@cms/modules/rpc/rpc-collection.js';
export interface CreateCollectionItemOptions {
    collection: string;
    onCreated?: (item: CollectionItemSummary) => void | Promise<void>;
}
export declare const openCollectionItemCreator: (options: CreateCollectionItemOptions) => Promise<void>;
export declare const runAction: (options: CreateCollectionItemOptions) => Promise<void>;
