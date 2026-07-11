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
    const iconEl = statusBtn.querySelector('#cms-btn-status-icon');
    if (!iconEl)
        return;
    // Alle cms-node-status-* Klassen entfernen
    Array.from(statusBtn.classList).forEach(className => {
        if (className.startsWith('workflow-status-button--')) {
            statusBtn.classList.remove(className);
        }
    });
    Array.from(iconEl.classList).forEach(className => {
        if (className.startsWith('bi-')) {
            iconEl.classList.remove(className);
        }
    });
    var published = statusResponse?.status.published;
    // Status bestimmen (Provider-fähig)
    let statusClass = "workflow-status-button--";
    let statusIcon = "";
    let statusText = "";
    if (!published) {
        statusClass += 'draft';
        statusIcon = "bi-pencil";
        statusText = "Draft";
    }
    else if (!statusResponse?.status.withinSchedule) {
        statusClass += 'scheduled';
        statusIcon = "bi-eye-slash";
        statusText = "Scheduled";
    }
    else {
        statusClass += 'visible';
        statusIcon = "bi-eye-fill";
        statusText = "Visible";
    }
    statusBtn.classList.add(statusClass);
    iconEl.classList.add(statusIcon);
    statusBtn.querySelector('#cms-btn-status-text').textContent = "";
}
