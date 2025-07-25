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
import { reloadPreview } from "../../js/modules/preview.utils";
import { setMeta } from "../../js/modules/rpc/rpc-content";
export async function runAction(params) {
    var request = {
        uri: params.sectionUri,
        meta: {
            published: {
                type: "select",
                value: params.published ? true : false,
            }
        }
    };
    var setMetaResponse = await setMeta(request);
    reloadPreview();
}
