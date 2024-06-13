import { $hooks } from 'system/hooks.mjs';
/*
legacy assets
theme.getAssets().addJs("/assets/form-1.js")
theme.getAssets().addCss("/assets/form-1.css")

theme.getAssets().addJs("/assets/app-1.js")
theme.getAssets().addCss("/assets/styles-1.css")
*/

$hooks.registerAction(
    "theme/header",
    (context) => {
        return `
            <link rel="stylesheet" href="/de/assets/form-1.css" defer />
            <link rel="stylesheet" href="/de/assets/styles-1.css" defer />
            <script src="/de/assets/form-1.js" defer></script>
            <script src="/de/assets/app-1.js" defer></script>
        `;
    }
);