export function openWizard(optionsParam: any): {
    wizardId: string;
    modalInstance: any;
    goToStep: (index: any) => void;
    getCurrentStep: () => number;
};
