/**
 * wardrobe.js — page controller for wardrobe.html
 * Reads/writes AppState. All data calls go through api.service.js.
 */
document.addEventListener('DOMContentLoaded', () => {

    // ── DOM refs ──────────────────────────────────────────────────────────────
    const grid              = document.getElementById('wardrobe-grid');
    const openModalBtn      = document.getElementById('open-add-modal-btn');
    const closeModalBtn     = document.getElementById('close-add-modal-btn');
    const cancelModalBtn    = document.getElementById('cancel-modal-btn');
    const addModal          = document.getElementById('add-item-modal');
    const addForm           = document.getElementById('add-clothing-form');
    const submitBtn         = document.getElementById('submit-add-btn');
    const dropzone          = document.getElementById('dropzone-area');
    const fileInput         = document.getElementById('clothing-file-input');
    const dropzonePreview   = document.getElementById('dropzone-preview');
    const searchInput       = document.getElementById('sidebar-search-input');
    const categoryList      = document.getElementById('category-filter-list');
    const conditionList     = document.getElementById('condition-filter-list');
    const modalCategoryList = document.getElementById('modal-category-list');
    const modalConditionList= document.getElementById('modal-condition-list');

    // ── Local state ───────────────────────────────────────────────────────────
    let uploadedImageSrc = '';

    // ── Bootstrap: load tags then load wardrobe items ─────────────────────────
    initPage();

    async function initPage() {
        await loadTags();
        await loadWardrobeItems();
    }

    // ── TAG LOADING ───────────────────────────────────────────────────────────
    async function loadTags() {
        AppState.tags.status = 'PENDING';
        try {
            const tags = await TagService.getTags();
            AppState.tags.categories = tags.categories;
            AppState.tags.conditions = tags.conditions;
            AppState.tags.status = 'SUCCESS';
            renderSidebarFilters();
            renderModalCheckboxes();
        } catch (err) {
            AppState.tags.status = 'ERROR';
            AppState.tags.error = err.message;
        }
    }

    function renderSidebarFilters() {
        // Categories — prepend "All Items" then each tag
        categoryList.innerHTML = `
            <label>
                <input type="checkbox" name="sidebar-category" value="all" checked> All Items
            </label>
        `;
        AppState.tags.categories.forEach(cat => {
            categoryList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="sidebar-category" value="${cat}"> ${cat}
                </label>
            `);
        });

        // Conditions
        conditionList.innerHTML = '';
        AppState.tags.conditions.forEach(cond => {
            conditionList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="sidebar-condition" value="${cond}"> ${cond}
                </label>
            `);
        });

        attachFilterListeners();
    }

    function renderModalCheckboxes() {
        // Category checkboxes in add modal (single-select via radio-like behaviour)
        modalCategoryList.innerHTML = '';
        AppState.tags.categories.forEach(cat => {
            modalCategoryList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="modal-category" value="${cat}"> ${cat}
                </label>
            `);
        });

        // Condition checkboxes in add modal (single-select)
        modalConditionList.innerHTML = '';
        AppState.tags.conditions.forEach(cond => {
            modalConditionList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="modal-condition" value="${cond}"> ${cond}
                </label>
            `);
        });

        // Enforce single-select behaviour within each group
        enforceSingleSelect(modalCategoryList, 'modal-category');
        enforceSingleSelect(modalConditionList, 'modal-condition');
    }

    function enforceSingleSelect(container, name) {
        container.addEventListener('change', (e) => {
            if (e.target.name !== name) return;
            container.querySelectorAll(`input[name="${name}"]`).forEach(cb => {
                if (cb !== e.target) cb.checked = false;
            });
        });
    }

    // ── SIDEBAR FILTER LISTENERS ──────────────────────────────────────────────
    function attachFilterListeners() {
        categoryList.addEventListener('change', (e) => {
            if (e.target.name !== 'sidebar-category') return;
            if (e.target.value === 'all') {
                categoryList.querySelectorAll('input[name="sidebar-category"]').forEach(cb => {
                    cb.checked = cb.value === 'all';
                });
                AppState.wardrobe.activeCategory = 'all';
            } else {
                categoryList.querySelector('input[value="all"]').checked = false;
                AppState.wardrobe.activeCategory = e.target.checked ? e.target.value : 'all';
                if (!e.target.checked) {
                    categoryList.querySelector('input[value="all"]').checked = true;
                }
            }
            renderGrid();
        });

        conditionList.addEventListener('change', (e) => {
            if (e.target.name !== 'sidebar-condition') return;
            AppState.wardrobe.activeCondition = e.target.checked ? e.target.value : 'all';
            conditionList.querySelectorAll('input[name="sidebar-condition"]').forEach(cb => {
                if (cb !== e.target) cb.checked = false;
            });
            renderGrid();
        });

        searchInput.addEventListener('input', () => renderGrid());
    }

    // ── WARDROBE ITEM LOADING ─────────────────────────────────────────────────
    async function loadWardrobeItems() {
        AppState.wardrobe.status = 'PENDING';
        renderGrid(); // shows skeletons

        try {
            const items = await WardrobeService.getItems();
            AppState.wardrobe.items = items;
            AppState.wardrobe.status = 'SUCCESS';
        } catch (err) {
            AppState.wardrobe.status = 'ERROR';
            AppState.wardrobe.error = err.message;
        }

        renderGrid();
    }

    // ── GRID RENDERING ENGINE ─────────────────────────────────────────────────
    function renderGrid() {
        grid.innerHTML = '';

        if (AppState.wardrobe.status === 'PENDING') {
            for (let i = 0; i < 6; i++) {
                const sk = document.createElement('div');
                sk.className = 'skeleton-card';
                grid.appendChild(sk);
            }
            return;
        }

        if (AppState.wardrobe.status === 'ERROR') {
            grid.innerHTML = `
                <div class="wardrobe-error-state">
                    <span><i class="fa-solid fa-circle-exclamation"></i>&nbsp; ${AppState.wardrobe.error || 'Failed to load wardrobe.'}</span>
                    <button class="retry-btn" id="retry-load-btn">RETRY</button>
                </div>`;
            document.getElementById('retry-load-btn').addEventListener('click', loadWardrobeItems);
            return;
        }

        const query    = searchInput.value.trim().toLowerCase();
        const catFilter  = AppState.wardrobe.activeCategory;
        const condFilter = AppState.wardrobe.activeCondition;

        const filtered = AppState.wardrobe.items.filter(item => {
            const matchCat  = catFilter  === 'all' || item.category  === catFilter;
            const matchCond = condFilter === 'all' || item.condition === condFilter;
            const matchQ    = !query || item.name.toLowerCase().includes(query) || item.category.toLowerCase().includes(query);
            return matchCat && matchCond && matchQ;
        });

        if (filtered.length === 0) {
            grid.innerHTML = `
                <div class="wardrobe-empty-state">
                    <i class="fa-regular fa-folder-open"></i>
                    <p>No items found.</p>
                    <span>Try adjusting your filters or add a new item.</span>
                </div>`;
            return;
        }

        filtered.forEach(item => {
            const card = document.createElement('div');
            card.className = 'clothing-card-v2';
            card.dataset.id = item.id;
            card.innerHTML = `
                <div class="card-img-wrap">
                    <button class="card-delete-btn" data-id="${item.id}" title="Delete item">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                    ${item.imageSrc
                        ? `<img src="${item.imageSrc}" alt="${item.name}">`
                        : `<div class="card-img-placeholder"><i class="fa-regular fa-image"></i></div>`
                    }
                </div>
                <div class="card-body">
                    <span class="card-category-tag">${item.tags.join(' · ')}</span>
                    <h3 class="card-name">${item.name}</h3>
                    <div class="card-meta-row">
                        <span class="card-condition">${item.condition}</span>
                        <span class="card-formality-badge">F${item.tokenFormalitas}</span>
                    </div>
                </div>`;
            grid.appendChild(card);
        });

        // Delete button delegation
        grid.querySelectorAll('.card-delete-btn').forEach(btn => {
            btn.addEventListener('click', () => handleDelete(btn.dataset.id));
        });
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    async function handleDelete(id) {
        if (!confirm('Remove this item from your wardrobe?')) return;
        try {
            await WardrobeService.deleteItem(id);
            AppState.wardrobe.items = AppState.wardrobe.items.filter(i => i.id !== id);
            renderGrid();
        } catch (err) {
            alert('Could not delete item: ' + err.message);
        }
    }

    // ── MODAL OPEN / CLOSE ────────────────────────────────────────────────────
    const openModal  = () => addModal.classList.add('active');
    const closeModal = () => {
        addModal.classList.remove('active');
        addForm.reset();
        resetDropzone();
        setSubmitIdle();
    };

    openModalBtn.addEventListener('click', openModal);
    closeModalBtn.addEventListener('click', closeModal);
    cancelModalBtn.addEventListener('click', closeModal);
    addModal.addEventListener('click', (e) => { if (e.target === addModal) closeModal(); });

    // ── DROPZONE ──────────────────────────────────────────────────────────────
    dropzone.addEventListener('click', () => fileInput.click());
    dropzone.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') fileInput.click(); });

    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;
        uploadedImageSrc = URL.createObjectURL(file);
        dropzonePreview.src = uploadedImageSrc;
        dropzonePreview.classList.add('visible');
        dropzone.querySelector('i').style.display = 'none';
        dropzone.querySelector('p').style.display = 'none';
        dropzone.querySelector('span').style.display = 'none';
    });

    // Drag-and-drop support
    dropzone.addEventListener('dragover', (e) => { e.preventDefault(); dropzone.style.background = '#f0f0f0'; });
    dropzone.addEventListener('dragleave', () => { dropzone.style.background = ''; });
    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.style.background = '';
        const file = e.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            fileInput.files = e.dataTransfer.files;
            fileInput.dispatchEvent(new Event('change'));
        }
    });

    function resetDropzone() {
        uploadedImageSrc = '';
        dropzonePreview.src = '';
        dropzonePreview.classList.remove('visible');
        dropzone.querySelector('i').style.display = '';
        dropzone.querySelector('p').style.display = '';
        dropzone.querySelector('span').style.display = '';
        fileInput.value = '';
    }

    // ── SUBMIT STATE HELPERS ──────────────────────────────────────────────────
    function setSubmitPending() {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="btn-spinner"></span> SAVING...';
    }

    function setSubmitIdle() {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'ADD TO WARDROBE';
    }

    function setSubmitError() {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'TRY AGAIN';
    }

    // ── FORM SUBMIT ───────────────────────────────────────────────────────────
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name       = document.getElementById('item-name').value.trim();
        const formality  = parseInt(document.getElementById('item-formality').value, 10);
        const categoryEl = modalCategoryList.querySelector('input[name="modal-category"]:checked');
        const conditionEl= modalConditionList.querySelector('input[name="modal-condition"]:checked');

        if (!name) { alert('Please enter an item name.'); return; }
        if (!categoryEl) { alert('Please select a category.'); return; }
        if (!conditionEl) { alert('Please select a condition.'); return; }
        if (!formality || formality < 1 || formality > 5) { alert('Please enter a formality token between 1 and 5.'); return; }

        const category  = categoryEl.value;
        const condition = conditionEl.value;

        setSubmitPending();

        try {
            const newItem = await WardrobeService.addItem({
                name,
                category,
                condition,
                tokenFormalitas: formality,
                tags: [category],
                imageSrc: uploadedImageSrc,
                file: fileInput.files[0] || null
            });

            AppState.wardrobe.items.unshift(newItem);
            renderGrid();
            closeModal();
        } catch (err) {
            setSubmitError();
            alert('Failed to add item: ' + err.message);
        }
    });
});
