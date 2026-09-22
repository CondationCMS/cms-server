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
import { Form } from '@cms/modules/form/forms.js';
import { CollectionType } from '@cms/modules/rpc/rpc-manager.js';
export declare const collectionForm: (types: CollectionType[], collection: string, mode?: "create" | "edit") => any;
export interface EditCollectionItemOptions {
    collection: string;
    id: string;
    reloadAfterSave?: boolean;
    onSaved?: () => void | Promise<void>;
}
export interface CollectionItemEditor {
    form: Form;
    save: () => Promise<boolean>;
}
export declare const createCollectionItemEditor: (options: EditCollectionItemOptions) => Promise<CollectionItemEditor>;
export declare const openCollectionItemEditor: (options: EditCollectionItemOptions) => Promise<void>;
export declare const runAction: (options: EditCollectionItemOptions) => Promise<void>;
