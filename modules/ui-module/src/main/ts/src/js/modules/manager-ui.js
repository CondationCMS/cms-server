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
import { getWfManagerStatus } from './rpc/rpc-workflow';
import { executeScriptAction } from '../manager-globals';
import { getActivePreviewContent } from './preview-context.js';

const updateVariantBadge = (content) => {
  const badge = document.querySelector('#cms-current-variant');
  const label = document.querySelector('#cms-current-variant-label');
  if (!badge || !label) {
    return;
  }

  const variantId = content?.variantId;
  label.textContent = content ? (variantId || 'Original') : 'Loading…';
  badge.disabled = !content?.uri;
  badge.classList.toggle('text-bg-warning', Boolean(variantId));
  badge.classList.toggle('text-bg-secondary', !variantId);
  badge.setAttribute(
    'title',
    variantId ? `Current variant: ${variantId}` : (content ? 'Original page' : 'Loading preview content')
  );
};

window.addEventListener('cms:preview-context-changed', (event) => {
  updateVariantBadge(event.detail);
});

document.addEventListener('DOMContentLoaded', () => {
  const badge = document.querySelector('#cms-current-variant');
  badge?.addEventListener('click', () => {
    executeScriptAction({
      module: window.manager.baseUrl + '/actions/page/variants',
      function: 'runAction',
      parameters: {}
    });
  });
  updateVariantBadge(getActivePreviewContent());
});

export function updateStateButton() {
  const previewUrl = getPreviewUrl();
  const activePreviewContent = getActivePreviewContent(previewUrl);
  if (!previewUrl || !activePreviewContent?.uri) {
    const statusButton = document.querySelector('#cms-btn-status');
    statusButton?.classList.add('disabled');
    statusButton?.setAttribute('title', 'No preview content available');
    return;
  }

  getWfManagerStatus({}).then((getStatusResponse) => {
    updateNodeStatus(getStatusResponse);
  }).catch(() => {
    hideStatusButton();
  });
}

function hideStatusButton() {
  const statusBtn = document.querySelector('#cms-btn-status');
  if (statusBtn) {
    statusBtn.classList.add('disabled');
  }
}

function updateNodeStatus(statusResponse) {
  const statusBtn = document.querySelector('#cms-btn-status');
  if (!statusBtn) return;
  const iconEl = statusBtn.querySelector('#cms-btn-status-icon');
  if (!iconEl) return;

  if (!statusResponse?.status) {
    hideStatusButton();
    return;
  }

  statusBtn.classList.remove('disabled');

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
  let statusIcon = ""
  let statusText = ""
  if (!published) {
    statusClass += 'draft';
    statusIcon = "bi-pencil"
    statusText = "Draft"
  } else if (!statusResponse?.status.withinSchedule) {
    statusClass += 'scheduled';
    statusIcon = "bi-eye-slash"
    statusText = "Scheduled"
  } else {
    statusClass += 'visible';
    statusIcon = "bi-eye-fill"
    statusText = "Visible"
  }

  statusBtn.classList.add(statusClass);
  iconEl.classList.add(statusIcon);
  statusBtn.querySelector('#cms-btn-status-text').textContent = statusText;
  
  updateWorkflowStatus(statusResponse);
}

const updateWorkflowStatus = (statusResponse) => {

  let visibilityStatus = document.querySelector('#cms-workflow-visibility');
  Array.from(visibilityStatus.classList).forEach(className => {
    if (className.startsWith('bi-')) {
      visibilityStatus.classList.remove(className);
    }
  });

  let statusClass = "workflow-status-button--";
  var published = statusResponse?.status.published;
  let visibilityText = ""
  if (!published) {
    visibilityText = "Not visible"
    statusClass += 'draft';
  } else if (!statusResponse?.status.withinSchedule) {
    visibilityText = "Not visible"
    statusClass += 'scheduled';
  } else {
    visibilityText = "Visible"
    statusClass += 'visible';
  }

  document.querySelector('#cms-workflow-stage').textContent = statusResponse?.status.currentStage || '---';
  visibilityStatus.textContent = visibilityText;
  visibilityStatus.classList.add(statusClass);

  const formatter = new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short"
  });
  if (statusResponse?.status.publish_date ) {
    document.querySelector('#cms-workflow-visibility-since').textContent = formatter.format(new Date(statusResponse?.status.publish_date));
  } else {
    document.querySelector('#cms-workflow-visibility-since').textContent = "---";
  }
  if (statusResponse?.status.unpublish_date ) {
    document.querySelector('#cms-workflow-visibility-until').textContent = formatter.format(new Date(statusResponse?.status.unpublish_date));
  } else {
    document.querySelector('#cms-workflow-visibility-until').textContent = "---";
  }

  const wfTransitionsContainer = document.querySelector('#cms-workflow-transitions-container');
  const transitions = statusResponse?.transitions || [];
  wfTransitionsContainer.innerHTML = transitions.map(transitionButton).join('');

  wfTransitionsContainer.querySelectorAll('.workflow-transition').forEach((btn, index) => {
    btn.addEventListener('click', () => {
      const transition = transitions[index];
      executeTransition(transition.id);
    });
  });
}

const executeTransition = async (transitionId) => {
    var cmd = {
        "module": window.manager.baseUrl + "/actions/page/wf-run-transition",
        "function": "runAction",
        "parameters": {
            "transitionId": transitionId
        }
    }
    executeScriptAction(cmd)
}

const transitionButton = (transition) => {
  return `
    <button class="dropdown-item workflow-transition" type="button">
      <span class="workflow-transition__content">
          <span class="workflow-transition__label">
              ${transition.label}
          </span>

          <small class="workflow-transition__description">
              ${transition.description}
          </small>
      </span>
    </button>
  `;
}
