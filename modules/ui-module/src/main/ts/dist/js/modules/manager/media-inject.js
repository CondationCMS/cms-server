import { EDIT_ATTRIBUTES_ICON } from "./toolbar-icons";
import frameMessenger from '../frameMessenger.js';
const isSameDomainImage = (imgElement) => {
    if (!(imgElement instanceof HTMLImageElement)) {
        return false; // ist kein <img>
    }
    if (!imgElement.src) {
        return false;
    }
    try {
        const imgUrl = new URL(imgElement.src, window.location.href);
        return imgUrl.hostname === window.location.hostname;
    }
    catch (e) {
        return false;
    }
};
export const initMediaUploadOverlay = (img) => {
    if (!isSameDomainImage(img)) {
        return;
    }
    // Overlay erstellen
    const overlay = document.createElement('div');
    overlay.classList.add("cms-ui-overlay-bottom");
    overlay.innerText = "Bild austauschen…";
    document.body.appendChild(overlay);
    const positionOverlay = () => {
        const rect = img.getBoundingClientRect();
        const overlayHeight = rect.height / 3; // unteres Drittel
        overlay.style.top = `${window.scrollY + rect.top + rect.height - overlayHeight}px`;
        overlay.style.left = `${window.scrollX + rect.left}px`;
        overlay.style.width = `${rect.width}px`;
        overlay.style.height = `${overlayHeight}px`;
    };
    img.addEventListener('mouseenter', () => {
        positionOverlay();
        overlay.classList.add('visible');
    });
    img.addEventListener('mouseleave', (event) => {
        if (!event.relatedTarget || !(event.relatedTarget instanceof Node) || !overlay.contains(event.relatedTarget)) {
            overlay.classList.remove('visible');
        }
    });
    overlay.addEventListener('mouseleave', (event) => {
        if (!event.relatedTarget || event.relatedTarget !== img) {
            overlay.classList.remove('visible');
        }
    });
    overlay.addEventListener('click', (e) => {
        selectMedia(e);
    });
    window.addEventListener('scroll', () => {
        if (overlay.classList.contains('visible'))
            positionOverlay();
    });
    window.addEventListener('resize', () => {
        if (overlay.classList.contains('visible'))
            positionOverlay();
    });
    positionOverlay();
};
export const initMediaToolbar = (img) => {
    if (!isSameDomainImage(img)) {
        return;
    }
    const toolbar = document.createElement('div');
    toolbar.classList.add("cms-ui-toolbar");
    toolbar.classList.add("cms-ui-toolbar-tl");
    const button = document.createElement('button');
    button.setAttribute('data-cms-action', 'editMediaForm');
    button.setAttribute('data-cms-media-form', 'meta');
    button.innerHTML = EDIT_ATTRIBUTES_ICON;
    button.setAttribute("title", "Edit attributes");
    button.addEventListener('click', (event) => {
        editMediaForm(event, "meta", img.src);
    });
    toolbar.appendChild(button);
    document.body.appendChild(toolbar);
    const positionToolbar = () => {
        const rect = img.getBoundingClientRect();
        toolbar.style.top = `${window.scrollY + rect.top}px`;
        toolbar.style.left = `${window.scrollX + rect.left}px`;
    };
    img.addEventListener('mouseenter', () => {
        positionToolbar();
        //toolbar.style.display = 'block';
        toolbar.classList.add('visible');
    });
    img.addEventListener('mouseleave', (event) => {
        // nur ausblenden, wenn die Maus nicht gerade über der Toolbar ist
        if (!event.relatedTarget || !toolbar.contains(event.relatedTarget)) {
            //toolbar.style.display = 'none';
            toolbar.classList.remove('visible');
        }
    });
    toolbar.addEventListener('mouseleave', (event) => {
        if (!event.relatedTarget || event.relatedTarget !== img) {
            //toolbar.style.display = 'none';
            toolbar.classList.remove('visible');
        }
    });
    window.addEventListener('scroll', () => {
        if (toolbar.style.visibility === 'visible')
            positionToolbar();
    });
    window.addEventListener('resize', () => {
        if (toolbar.style.visibility === 'visible')
            positionToolbar();
    });
};
const selectMedia = (event) => {
    var command = {
        type: 'edit',
        payload: {
            editor: "select",
            element: "image",
            options: {
                metaElement: event.target.dataset.cmsMetaElement,
            }
        }
    };
    frameMessenger.send(window.parent, command);
};
const editMediaForm = (event, form, image) => {
    var command = {
        type: 'edit',
        payload: {
            editor: "form",
            element: "image",
            options: {
                form: form,
                image: image
            }
        }
    };
    frameMessenger.send(window.parent, command);
};
