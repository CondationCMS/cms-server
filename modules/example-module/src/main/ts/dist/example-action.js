import { showToast } from 'condation-cms-ui/dist/js/modules/toast.js';
export async function runAction(parameters) {
    console.log("This is an example action");
    showToast({
        title: 'Example Action',
        message: 'Example Action executed successfully!',
        type: 'success',
        duration: 3000
    });
}
