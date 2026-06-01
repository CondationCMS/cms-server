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

import { i18n } from "@cms/modules/localization.js";

const defaultOptions = {
	title: 'Wizard',
	fullscreen: false,
	size: null,
	showStepIndicator: true,
	nextLabel: () => i18n.t('wizard.buttons.next', 'Next'),
	prevLabel: () => i18n.t('wizard.buttons.previous', 'Previous'),
	finishLabel: () => i18n.t('wizard.buttons.finish', 'Finish'),
	cancelLabel: () => i18n.t('wizard.buttons.cancel', 'Cancel'),
	validateStep: () => true,
};

const renderStepBody = (step, containerId) => {
	const container = document.getElementById(containerId);
	if (!container) return;
	container.innerHTML = '';

	if (typeof step.body === 'function') {
		const bodyResult = step.body();
		if (typeof bodyResult === 'string') {
			container.innerHTML = bodyResult;
		} else if (bodyResult instanceof HTMLElement) {
			container.appendChild(bodyResult);
		} else if (bodyResult && typeof bodyResult.then === 'function') {
			bodyResult.then((result) => {
				container.innerHTML = typeof result === 'string' ? result : '';
				if (result instanceof HTMLElement) {
					container.appendChild(result);
				}
			});
		}
	} else if (step.body instanceof HTMLElement) {
		container.appendChild(step.body);
	} else {
		container.innerHTML = step.body || '';
	}
};

const renderStepIndicator = (steps, currentStep, indicatorContainer) => {
	if (!indicatorContainer) return;
	indicatorContainer.innerHTML = '';
	steps.forEach((step, index) => {
		const stepNode = document.createElement('div');
		stepNode.className = `wizard-step-item ${index === currentStep ? 'active' : index < currentStep ? 'completed' : ''}`;
		stepNode.innerHTML = `
			<div class="wizard-step-number">${index + 1}</div>
			<div class="wizard-step-title">${step.title || i18n.t('wizard.step', 'Step')} ${index + 1}</div>
		`;
		indicatorContainer.appendChild(stepNode);
	});
};

const openWizard = (optionsParam) => {
	const wizardId = 'wizard_' + Date.now();

	const options = {
		...defaultOptions,
		...optionsParam,
	};

	const steps = Array.isArray(options.steps) ? options.steps : [];
	let currentStep = 0;

	let fullscreen = '';
	if (options.fullscreen) {
		fullscreen = 'modal-fullscreen';
	}

	let size = '';
	if (options.size) {
		size = 'modal-' + options.size;
	}

	const modalHtml = `
		<div class="modal fade" id="${wizardId}" tabindex="-1" aria-hidden="true">
		  <div class="modal-dialog ${fullscreen} ${size}">
			<div class="modal-content">
			  <div class="modal-header">
				<h5 class="modal-title">${options.title}</h5>
				<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			  </div>
							<div class="modal-body">
								${options.showStepIndicator ? `<div class="wizard-step-indicator" id="${wizardId}_stepIndicator"></div><div class="wizard-step-title d-none" id="${wizardId}_stepTitle"></div>` : `<div class="wizard-step-title" id="${wizardId}_stepTitle"></div>`}
								<div id="${wizardId}_bodyContainer"></div>
							</div>
			  <div class="modal-footer">
				<button type="button" class="btn btn-secondary" id="${wizardId}_prevBtn">${options.prevLabel()}</button>
				<button type="button" class="btn btn-secondary" id="${wizardId}_cancelBtn">${options.cancelLabel()}</button>
				<button type="button" class="btn btn-primary" id="${wizardId}_nextBtn">${options.nextLabel()}</button>
			  </div>
			</div>
		  </div>
		</div>`;

	const container = document.getElementById('modalContainer');
	const modalDiv = document.createElement('div');
	modalDiv.innerHTML = modalHtml.trim();
	const modalNode = modalDiv.firstChild;
	container.appendChild(modalNode);

	const modalElement = document.getElementById(wizardId);
	const modalInstance = new bootstrap.Modal(modalElement, {
		backdrop: 'static',
		keyboard: true,
		focus: true,
	});

	const prevBtn = document.getElementById(`${wizardId}_prevBtn`);
	const nextBtn = document.getElementById(`${wizardId}_nextBtn`);
	const cancelBtn = document.getElementById(`${wizardId}_cancelBtn`);
	const stepTitle = document.getElementById(`${wizardId}_stepTitle`);
	const stepIndicator = options.showStepIndicator ? document.getElementById(`${wizardId}_stepIndicator`) : null;
	const bodyContainerId = `${wizardId}_bodyContainer`;

	const updateButtons = () => {
		prevBtn.style.display = currentStep === 0 ? 'none' : '';
		nextBtn.textContent = currentStep === steps.length - 1 ? options.finishLabel() : options.nextLabel();
	};

	const renderStep = () => {
		const step = steps[currentStep] || {};
		const titleText = step.title || `${i18n.t('wizard.step', 'Step')} ${currentStep + 1}`;
		if (stepTitle) {
			stepTitle.textContent = titleText;
		}
		renderStepBody(step, bodyContainerId);
		if (options.showStepIndicator && stepIndicator) {
			renderStepIndicator(steps, currentStep, stepIndicator);
		}
		updateButtons();
		if (typeof step.onShow === 'function') {
			step.onShow(modalElement);
		}
	};

	const goToStep = (index) => {
		const step = steps[currentStep] || {};
		if (typeof step.validate === 'function' && !step.validate()) {
			return;
		}
		if (typeof options.validateStep === 'function' && !options.validateStep(currentStep)) {
			return;
		}
		if (currentStep !== index && typeof step.onHide === 'function') {
			step.onHide(modalElement);
		}
		currentStep = Math.max(0, Math.min(index, steps.length - 1));
		renderStep();
		if (typeof options.onStepChange === 'function') {
			options.onStepChange(currentStep);
		}
	};

	prevBtn.addEventListener('click', () => goToStep(currentStep - 1));
	cancelBtn.addEventListener('click', () => {
		modalInstance.hide();
		if (typeof options.onCancel === 'function') {
			options.onCancel();
		}
	});
	nextBtn.addEventListener('click', () => {
		const step = steps[currentStep] || {};
		const valid = typeof step.validate === 'function' ? step.validate() : true;
		if (!valid) {
			return;
		}
		if (currentStep === steps.length - 1) {
			modalInstance.hide();
			if (typeof options.onFinish === 'function') {
				options.onFinish();
			}
			return;
		}
		goToStep(currentStep + 1);
	});

	modalElement.addEventListener('shown.bs.modal', () => {
		renderStep();
		if (typeof options.onShow === 'function') {
			options.onShow(modalElement);
		}
	});

	modalElement.addEventListener('hidden.bs.modal', () => {
		modalNode.remove();
		if (typeof options.onClose === 'function') {
			options.onClose();
		}
	});

	modalInstance.show();

	return {
		wizardId,
		modalInstance,
		goToStep,
		getCurrentStep: () => currentStep,
	};
};

export { openWizard };
