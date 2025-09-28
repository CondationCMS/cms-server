// Minimaler JS-Code
const list = document.getElementById('object-list');
const addBtn = document.getElementById('add-object');

// Element hinzufügen
addBtn.addEventListener('click', () => {
    const item = document.createElement('div');
    item.className = 'list-group-item d-flex justify-content-between align-items-center';
    item.innerHTML = `
      <span class="object-name flex-grow-1">Neues Objekt</span>
      <button class="btn btn-sm btn-outline-danger ms-2 remove-btn" title="Entfernen">
        <i class="bi bi-x-lg"></i>
      </button>
    `;
    list.appendChild(item);
});

// Entfernen
list.addEventListener('click', e => {
    if (e.target.closest('.remove-btn')) {
        e.target.closest('.list-group-item').remove();
    }
});

// Doppelklick bearbeiten
list.addEventListener('dblclick', e => {
    const span = e.target.closest('.object-name');
    if (span) {
        const oldText = span.textContent.trim();
        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'form-control form-control-sm';
        input.value = oldText;
        span.replaceWith(input);
        input.focus();
        input.addEventListener('blur', () => {
            const newSpan = document.createElement('span');
            newSpan.className = 'object-name flex-grow-1';
            newSpan.textContent = input.value || oldText;
            input.replaceWith(newSpan);
        });
    }
});