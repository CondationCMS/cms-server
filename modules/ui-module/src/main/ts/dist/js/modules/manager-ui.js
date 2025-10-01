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
import { getContentNode, setMeta, getContent } from './rpc/rpc-content.js';
import { getPreviewUrl } from './preview.utils.js';
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
        getContent({
            uri: contentNode.result.uri
        }).then((getContentResponse) => {
            var published = getContentResponse?.result?.meta?.published;
            if (published) {
                if (isPagePublishedExpired(getContentResponse)) {
                    document.querySelector('#cms-btn-status').classList.remove('btn-warning');
                    document.querySelector('#cms-btn-status').classList.remove('btn-success');
                    document.querySelector('#cms-btn-status').classList.add('btn-info');
                }
                else {
                    document.querySelector('#cms-btn-status').classList.remove('btn-warning');
                    document.querySelector('#cms-btn-status').classList.remove('btn-info');
                    document.querySelector('#cms-btn-status').classList.add('btn-success');
                }
            }
            else {
                document.querySelector('#cms-btn-status').classList.remove('btn-success');
                document.querySelector('#cms-btn-status').classList.remove('btn-info');
                document.querySelector('#cms-btn-status').classList.add('btn-warning');
            }
        });
    });
}
export function isPagePublishedExpired(contentResponse) {
    const publishDateStr = contentResponse?.result?.meta?.publish_date;
    const unpublishDateStr = contentResponse?.result?.meta?.unpublish_date;
    const now = new Date();
    const publishDate = publishDateStr ? new Date(publishDateStr) : null;
    const unpublishDate = unpublishDateStr ? new Date(unpublishDateStr) : null;
    // page is published if:
    // - publishDate empty or in the past
    // - und unpublishDate empty or in the future
    const isPublished = (!publishDate || publishDate <= now) &&
        (!unpublishDate || unpublishDate > now);
    return !isPublished;
}
