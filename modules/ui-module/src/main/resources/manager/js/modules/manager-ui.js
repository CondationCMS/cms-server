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
import { getPreviewUrl } from '@cms/modules/preview.utils.js';
import { getContent, getContentNode } from '@cms/modules/rpc/rpc-content.js';
import { getWfStatus } from './rpc/rpc-workflow';
export function updateStateButton() {
    var previewUrl = getPreviewUrl();
    ;
    if (!previewUrl) {
        document.querySelector('#cms-btn-status').classList.add('disabled');
        document.querySelector('#cms-btn-status').setAttribute('title', 'No preview URL available');
        return;
    }
    var previewUrl = getPreviewUrl();
    getContentNode({
        url: previewUrl
    }).then((contentNode) => {
        getWfStatus({
            uri: contentNode.result.uri
        }).then((getStatusResponse) => {
            updateNodeStatus(getStatusResponse);
        });
    });
}
function updateNodeStatus(statusResponse) {
    const statusBtn = document.querySelector('#cms-btn-status');
    if (!statusBtn)
        return;
    // Alle cms-node-status-* Klassen entfernen
    Array.from(statusBtn.classList).forEach(className => {
        if (className.startsWith('cms-node-status-')) {
            statusBtn.classList.remove(className);
        }
    });
    var published = statusResponse?.status.published;
    // Status bestimmen (Provider-fähig)
    let status;
    if (!published) {
        status = 'unpublished';
    }
    else if (!statusResponse?.status.withinSchedule) {
        status = 'published-not-visible';
    }
    else {
        status = 'published';
    }
    statusBtn.classList.add(`cms-node-status-${status}`);
}
