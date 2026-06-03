document.addEventListener('DOMContentLoaded', () => {

    // ==========================================================================
    // PAGE CHECK: ONLY RUN SLIDER IF ON THE HOME / LANDING PAGE
    // ==========================================================================
    if (document.querySelector('.slides-wrapper') || document.querySelector('.next-btn')) {
        
        // This is your EXACT original code, untouched
        const slidesWrapper = document.querySelector('.slides-wrapper');
        const slides = document.querySelectorAll('.slide');
        const prevBtn = document.querySelector('.prev-btn');
        const nextBtn = document.querySelector('.next-btn');
        
        let currentIndex = 0;
        const totalSlides = slides.length;
        let autoSlideInterval;

        // Function to move to a specific slide
        const goToSlide = (index) => {
            // Handle looping around the ends
            if (index < 0) {
                currentIndex = totalSlides - 1;
            } else if (index >= totalSlides) {
                currentIndex = 0;
            } else {
                currentIndex = index;
            }
            
            // Move the wrapper
            const offset = -currentIndex * 100;
            slidesWrapper.style.transform = `translateX(${offset}%)`;
        };

        // Next slide function
        const nextSlide = () => {
            goToSlide(currentIndex + 1);
        };

        // Previous slide function
        const prevSlide = () => {
            goToSlide(currentIndex - 1);
        };

        // Start auto slider (changes slide every 5 seconds)
        const startAutoSlide = () => {
            autoSlideInterval = setInterval(nextSlide, 5000);
        };

        // Reset timer when user manually clicks to prevent sudden jumps
        const resetTimer = () => {
            clearInterval(autoSlideInterval);
            startAutoSlide();
        };

        // Event Listeners for manual buttons
        nextBtn.addEventListener('click', () => {
            nextSlide();
            resetTimer();
        });

        prevBtn.addEventListener('click', () => {
            prevSlide();
            resetTimer();
        });

        // Initialize auto slider
        startAutoSlide();
    }


    // ==========================================================================
    // PAGE CHECK: ONLY RUN WARDROBE IF ON WARDROBE PAGE
    // ==========================================================================
    if (document.getElementById('wardrobe-grid') || document.getElementById('open-add-modal-btn')) {
        
        const wardrobeGrid = document.getElementById('wardrobe-grid');
        const emptyState = document.getElementById('empty-state');
        const filterTabs = document.querySelectorAll('.filter-tab');
        
        // Modal Overlay Elements
        const addItemModal = document.getElementById('add-item-modal');
        const openModalBtn = document.getElementById('open-add-modal-btn');
        const closeModalX = document.getElementById('close-add-modal-btn');
        const cancelModalBtn = document.getElementById('cancel-modal-btn');
        
        // Form & Upload Elements
        const addClothingForm = document.getElementById('add-clothing-form');
        const dropzoneArea = document.getElementById('dropzone-area');
        const clothingFileInput = document.getElementById('clothing-file-input');
        const dropzoneText = document.getElementById('dropzone-text');
        const uploadPreviewImg = document.getElementById('upload-preview-img');

        // App State Variables
        let wardrobeData = [];
        let activeFilter = 'all';

        // Modal Open/Close Controls
        const openModal = () => {
            if (addItemModal) {
                addItemModal.classList.add('active');
            }
        };

        const closeModal = () => {
            if (addItemModal) {
                addItemModal.classList.remove('active');
            }
            if (addClothingForm) addClothingForm.reset();
            resetUploaderPreview();
        };

        if (openModalBtn) openModalBtn.addEventListener('click', openModal);
        if (closeModalX) closeModalX.addEventListener('click', closeModal);
        if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);
        
        if (addItemModal) {
            addItemModal.addEventListener('click', (e) => {
                if (e.target === addItemModal) closeModal();
            });
        }

        // Image Uploader Dynamic Preview Engine
        if (dropzoneArea && clothingFileInput) {
            dropzoneArea.addEventListener('click', () => clothingFileInput.click());
            
            clothingFileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    const tempObjectURL = URL.createObjectURL(file);
                    displayUploadedPreview(tempObjectURL);
                }
            });
        }

        const displayUploadedPreview = (srcUrl) => {
            if (uploadPreviewImg && dropzoneText) {
                uploadPreviewImg.src = srcUrl;
                uploadPreviewImg.classList.remove('upload-preview-hidden');
                dropzoneText.style.opacity = '0';
            }
        };

        const resetUploaderPreview = () => {
            if (uploadPreviewImg && dropzoneText) {
                uploadPreviewImg.src = "";
                uploadPreviewImg.classList.add('upload-preview-hidden');
                dropzoneText.style.opacity = '1';
            }
        };

        // Form Submit Action Handler
        if (addClothingForm) {
            addClothingForm.addEventListener('submit', (e) => {
                e.preventDefault();

                const newItem = {
                    id: 'item_' + Date.now(),
                    name: document.getElementById('item-name').value.trim(),
                    category: document.getElementById('item-category').value,
                    color: document.getElementById('item-color').value.trim(),
                    imageSrc: uploadPreviewImg ? uploadPreviewImg.src : ""
                };

                wardrobeData.push(newItem);
                renderWardrobeGrid();
                closeModal();
            });
        }

        // Deletion Handler
        window.deleteWardrobeItem = (itemId) => {
            wardrobeData = wardrobeData.filter(item => item.id !== itemId);
            renderWardrobeGrid();
        };

        // UI Grid Rendering Engine
        const renderWardrobeGrid = () => {
            if (!wardrobeGrid) return;
            const cards = wardrobeGrid.querySelectorAll('.clothing-card');
            cards.forEach(card => card.remove());

            const filteredItems = wardrobeData.filter(item => {
                if (activeFilter === 'all') return true;
                return item.category === activeFilter;
            });

            if (emptyState) {
                emptyState.style.display = filteredItems.length === 0 ? 'block' : 'none';
            }

            filteredItems.forEach(item => {
                const cardEl = document.createElement('div');
                cardEl.classList.add('clothing-card');
                cardEl.setAttribute('data-id', item.id);

                cardEl.innerHTML = `
                    <div class="card-image-wrap">
                        <button class="delete-item-badge-btn" onclick="deleteWardrobeItem('${item.id}')" title="Delete Item">
                            <i class="fa-solid fa-trash-can"></i>
                        </button>
                        <img src="${item.imageSrc}" alt="${item.name}">
                    </div>
                    <div class="card-details">
                        <span class="item-tag">${item.category}</span>
                        <h3>${item.name}</h3>
                        <p class="item-meta-color">${item.color}</p>
                    </div>
                `;
                wardrobeGrid.appendChild(cardEl);
            });
        };

        // Category Menu Filter Tabs Click Actions
        filterTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                filterTabs.forEach(t => t.classList.remove('active'));
                e.target.classList.add('active');
                
                activeFilter = e.target.getAttribute('data-category');
                renderWardrobeGrid();
            });
        });
    }
});