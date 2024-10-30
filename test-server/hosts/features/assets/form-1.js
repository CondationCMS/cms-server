// form.js
const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
const generateString = (length) => {
    let result = ''
    const charactersLength = characters.length
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength))
    }

    return result;
}

const validateCaptcha = async (event) => {
    event.preventDefault();
    let request = {
        code: document.getElementById("inputCaptcha").value,
        key: document.getElementById("captchaKey").value
    }

    const response = await fetch('/module/forms-module/captcha/validate', {
        method: 'POST',
        body: JSON.stringify(request)
    })

    const validationResponse = await response.json()

    if (!validationResponse.valid) {
        alert("captcha code is not valid")
        event.preventDefault()
        return false
    } else {
        console.log(event.target)
        event.target.submit()
        return true
    }
}

document.addEventListener("DOMContentLoaded", () => {
    if (document.getElementById("reloadCaptcha")) {
        document.getElementById("reloadCaptcha").addEventListener("click", () => {
            let href = new URL(document.getElementById("captchaImg").src)
            let key = generateString(8)
            href.searchParams.set('key', key)

            document.getElementById("captchaKey").value = key
            document.getElementById("captchaImg").src = href.toString()
        })
    }

})
