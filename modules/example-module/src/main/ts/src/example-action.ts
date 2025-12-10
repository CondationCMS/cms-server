
import { showToast } from 'condation-cms-ui/dist/js/modules/toast.js';

export async function runAction(parameters : any) : Promise<void> {
	console.log("This is an example action");

    showToast({
        title: 'Example Action',
        message: 'Example Action executed successfully!',
        type: 'success',
        duration: 3000
    })
}