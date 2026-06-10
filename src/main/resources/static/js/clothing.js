/**
 * clothing.js — page controller for clothing.html (individual garments).
 * All data calls go through ClothingService / TagService in api.service.js.
 */
document.addEventListener('DOMContentLoaded', () => {

    // — DOM refs —
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
    const genderList        = document.getElementById('gender-filter-list');
    const formalityList     = document.getElementById('formality-filter-list');
    const conditionList     = document.getElementById('condition-filter-list');
    const modalCategoryList = document.getElementById('modal-category-list');
    const modalGenderList   = document.getElementById('modal-gender-list');
    const modalConditionList= document.getElementById('modal-condition-list');
    const formalitySelect   = document.getElementById('item-formality');

    // Tags that belong in the dedicated Gender section, not the Category grid.
    const GENDER_TAGS = ['Men', 'Women', 'Unisex'];
    // Style tags now decided by the Formality dropdown — kept out of the Category grid.
    const STYLE_TAGS = ['Formal', 'Casual'];
    // Categories that don't fit a formal event; disabled when Formality >= FORMAL_LEVEL.
    const NON_FORMAL_CATEGORIES = ['Sportswear'];
    const FORMAL_LEVEL = 4;
    // Sidebar "Formality" section filters by the F1–F5 token shown on each card.
    const FORMALITY_LEVELS = [
        { value: 1, label: 'F1 · Loungewear' },
        { value: 2, label: 'F2 · Casual' },
        { value: 3, label: 'F3 · Smart casual' },
        { value: 4, label: 'F4 · Formal' },
        { value: 5, label: 'F5 · Very formal' }
    ];

    initPage();

    async function initPage() {
        await loadTags();
        await loadItems();
    }

    // — TAG LOADING —
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
        // Categories = garment types only; Gender and Formality get their own sections.
        const genderTags  = AppState.tags.categories.filter(t => GENDER_TAGS.includes(t));
        const garmentTags = AppState.tags.categories.filter(
            t => !GENDER_TAGS.includes(t) && !STYLE_TAGS.includes(t));

        const checkboxRow = (name, val, label) => `
            <label>
                <input type="checkbox" name="${name}" value="${val}"> ${label}
            </label>`;

        categoryList.innerHTML = `
            <label>
                <input type="checkbox" name="sidebar-category" value="all" checked> All Items
            </label>`;
        garmentTags.forEach(cat =>
            categoryList.insertAdjacentHTML('beforeend', checkboxRow('sidebar-category', cat, cat)));

        genderList.innerHTML = '';
        genderTags.forEach(g =>
            genderList.insertAdjacentHTML('beforeend', checkboxRow('sidebar-category', g, g)));

        // Formality filters by the F1–F5 token (item.tokenFormalitas), not a tag.
        formalityList.innerHTML = '';
        FORMALITY_LEVELS.forEach(lvl =>
            formalityList.insertAdjacentHTML('beforeend', checkboxRow('sidebar-formality', lvl.value, lvl.label)));

        conditionList.innerHTML = '';
        AppState.tags.conditions.forEach(cond => {
            conditionList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="sidebar-condition" value="${cond}"> ${cond}
                </label>`);
        });

        attachFilterListeners();
    }

    function renderModalCheckboxes() {
        // Gender → its own single-select section.
        const genderTags = AppState.tags.categories.filter(t => GENDER_TAGS.includes(t));
        // Category → garment types only (gender + style tags excluded). Multi-select.
        const categoryTags = AppState.tags.categories.filter(
            t => !GENDER_TAGS.includes(t) && !STYLE_TAGS.includes(t));

        modalGenderList.innerHTML = '';
        genderTags.forEach(g => {
            modalGenderList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="modal-gender" value="${g}"> ${g}
                </label>`);
        });

        modalCategoryList.innerHTML = '';
        categoryTags.forEach(cat => {
            modalCategoryList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="modal-category" value="${cat}"> ${cat}
                </label>`);
        });

        modalConditionList.innerHTML = '';
        AppState.tags.conditions.forEach(cond => {
            modalConditionList.insertAdjacentHTML('beforeend', `
                <label>
                    <input type="checkbox" name="modal-condition" value="${cond}"> ${cond}
                </label>`);
        });

        // Gender & condition stay single-select; category is now multi-select.
        enforceSingleSelect(modalGenderList, 'modal-gender');
        enforceSingleSelect(modalConditionList, 'modal-condition');

        applyFormalityConstraints();
    }

    function enforceSingleSelect(container, name) {
        container.addEventListener('change', (e) => {
            if (e.target.name !== name) return;
            container.querySelectorAll(`input[name="${name}"]`).forEach(cb => {
                if (cb !== e.target) cb.checked = false;
            });
        });
    }

    // Disable (and clear) categories that don't fit a formal event when Formality >= 4.
    function applyFormalityConstraints() {
        const formality = parseInt(formalitySelect.value, 10);
        const isFormal = formality >= FORMAL_LEVEL;
        modalCategoryList.querySelectorAll('input[name="modal-category"]').forEach(cb => {
            const disable = isFormal && NON_FORMAL_CATEGORIES.includes(cb.value);
            cb.disabled = disable;
            if (disable) cb.checked = false;
            cb.closest('label').classList.toggle('is-disabled', disable);
        });
    }

    formalitySelect.addEventListener('change', applyFormalityConstraints);

    // — SIDEBAR FILTER LISTENERS —
    // Categories and Gender both drive the single activeCategory (tag) filter,
    // so selecting any one tag clears the others across both sections.
    const categorySections = [categoryList, genderList];

    function onCategoryFilterChange(e) {
        if (e.target.name !== 'sidebar-category') return;
        const inputs = categorySections.flatMap(
            list => [...list.querySelectorAll('input[name="sidebar-category"]')]);
        const allItemsCb = categoryList.querySelector('input[value="all"]');

        if (e.target.value === 'all') {
            inputs.forEach(cb => { cb.checked = cb.value === 'all'; });
            AppState.wardrobe.activeCategory = 'all';
        } else {
            inputs.forEach(cb => { cb.checked = cb === e.target && e.target.checked; });
            if (e.target.checked) {
                AppState.wardrobe.activeCategory = e.target.value;
            } else {
                allItemsCb.checked = true;
                AppState.wardrobe.activeCategory = 'all';
            }
        }
        renderGrid();
    }

    function attachFilterListeners() {
        categorySections.forEach(list => list.addEventListener('change', onCategoryFilterChange));

        formalityList.addEventListener('change', (e) => {
            if (e.target.name !== 'sidebar-formality') return;
            AppState.wardrobe.activeFormality = e.target.checked ? parseInt(e.target.value, 10) : 'all';
            formalityList.querySelectorAll('input[name="sidebar-formality"]').forEach(cb => {
                if (cb !== e.target) cb.checked = false;
            });
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

    // — ITEM LOADING —
    async function loadItems() {
        AppState.wardrobe.status = 'PENDING';
        renderGrid();
        try {
            AppState.wardrobe.items = await ClothingService.getItems();
            AppState.wardrobe.status = 'SUCCESS';
        } catch (err) {
            AppState.wardrobe.status = 'ERROR';
            AppState.wardrobe.error = err.message;
        }
        renderGrid();
    }

    // — GRID RENDERING —
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
                    <span><i class="fa-solid fa-circle-exclamation"></i>&nbsp; ${AppState.wardrobe.error || 'Failed to load clothing.'}</span>
                    <button class="retry-btn" id="retry-load-btn">RETRY</button>
                </div>`;
            document.getElementById('retry-load-btn').addEventListener('click', loadItems);
            return;
        }

        const query      = searchInput.value.trim().toLowerCase();
        const catFilter  = AppState.wardrobe.activeCategory;
        const condFilter = AppState.wardrobe.activeCondition;
        const formFilter = AppState.wardrobe.activeFormality;

        const filtered = AppState.wardrobe.items.filter(item => {
            const matchCat  = catFilter  === 'all' || item.tags.includes(catFilter);
            const matchCond = condFilter === 'all' || item.condition === condFilter;
            const matchForm = formFilter === 'all' || item.tokenFormalitas === formFilter;
            const matchQ    = !query || item.name.toLowerCase().includes(query);
            return matchCat && matchCond && matchForm && matchQ;
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
                    <button class="card-fav-btn ${item.favorite ? 'active' : ''}" data-id="${item.id}" title="Favorite"
                            style="position:absolute;top:8px;left:8px;border:none;background:rgba(255,255,255,.9);border-radius:50%;width:30px;height:30px;cursor:pointer;color:${item.favorite ? '#e63946' : '#bbb'};">
                        <i class="fa-solid fa-heart"></i>
                    </button>
                    <button class="card-delete-btn" data-id="${item.id}" title="Delete item">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                    ${item.imageSrc
                        ? `<img src="${item.imageSrc}" alt="${item.name}">`
                        : `<div class="card-img-placeholder"><i class="fa-regular fa-image"></i></div>`}
                </div>
                <div class="card-body">
                    <span class="card-category-tag">${item.tags.join(' · ')}</span>
                    <h3 class="card-name">${item.name}</h3>
                    <div class="card-meta-row">
                        <span class="card-formality-badge">F${item.tokenFormalitas}</span>
                    </div>
                </div>`;
            grid.appendChild(card);
        });

        grid.querySelectorAll('.card-delete-btn').forEach(btn => {
            btn.addEventListener('click', () => handleDelete(btn.dataset.id));
        });
        grid.querySelectorAll('.card-fav-btn').forEach(btn => {
            btn.addEventListener('click', () => handleFavorite(btn.dataset.id));
        });
    }

    // — FAVORITE —
    async function handleFavorite(id) {
        try {
            const updated = await ClothingService.toggleFavorite(id);
            const item = AppState.wardrobe.items.find(i => String(i.id) === String(id));
            if (item) item.favorite = updated.favorite;
            renderGrid();
        } catch (err) {
            alert('Could not update favorite: ' + err.message);
        }
    }

    // — DELETE —
    async function handleDelete(id) {
        if (!confirm('Remove this item from your clothing?')) return;
        try {
            await ClothingService.deleteItem(id);
            AppState.wardrobe.items = AppState.wardrobe.items.filter(i => String(i.id) !== String(id));
            renderGrid();
        } catch (err) {
            alert('Could not delete item: ' + err.message);
        }
    }

    // — MODAL OPEN / CLOSE —
    const openModal  = () => addModal.classList.add('active');
    const closeModal = () => {
        addModal.classList.remove('active');
        addForm.reset();
        resetDropzone();
        setSubmitIdle();
        applyFormalityConstraints();   // reset() clears the dropdown → re-enable all categories
    };

    openModalBtn.addEventListener('click', openModal);
    closeModalBtn.addEventListener('click', closeModal);
    cancelModalBtn.addEventListener('click', closeModal);
    addModal.addEventListener('click', (e) => { if (e.target === addModal) closeModal(); });

    // — DROPZONE —
    dropzone.addEventListener('click', () => fileInput.click());
    dropzone.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') fileInput.click(); });

    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;
        dropzonePreview.src = URL.createObjectURL(file);
        dropzonePreview.classList.add('visible');
        dropzone.querySelector('i').style.display = 'none';
        dropzone.querySelector('p').style.display = 'none';
        dropzone.querySelector('span').style.display = 'none';
    });

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
        dropzonePreview.src = '';
        dropzonePreview.classList.remove('visible');
        dropzone.querySelector('i').style.display = '';
        dropzone.querySelector('p').style.display = '';
        dropzone.querySelector('span').style.display = '';
        fileInput.value = '';
    }

    // — SUBMIT STATE HELPERS —
    function setSubmitPending() { submitBtn.disabled = true;  submitBtn.innerHTML = '<span class="btn-spinner"></span> SAVING...'; }
    function setSubmitIdle()    { submitBtn.disabled = false; submitBtn.innerHTML = 'ADD TO CLOTHING'; }
    function setSubmitError()   { submitBtn.disabled = false; submitBtn.innerHTML = 'TRY AGAIN'; }

    // — FORM SUBMIT —
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name        = document.getElementById('item-name').value.trim();
        const formality   = parseInt(formalitySelect.value, 10);
        const genderEl    = modalGenderList.querySelector('input[name="modal-gender"]:checked');
        const categoryEls = modalCategoryList.querySelectorAll('input[name="modal-category"]:checked');

        if (!name) { alert('Please enter an item name.'); return; }
        if (!formality || formality < 1 || formality > 5) { alert('Please select a formality level.'); return; }
        if (!genderEl) { alert('Please select a gender.'); return; }
        if (categoryEls.length === 0) { alert('Please select at least one category.'); return; }

        const tags = [genderEl.value, ...Array.from(categoryEls, el => el.value)];
        setSubmitPending();

        try {
            const newItem = await ClothingService.addItem({
                name,
                tokenFormalitas: formality,
                tags,
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
