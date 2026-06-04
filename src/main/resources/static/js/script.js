document.addEventListener('DOMContentLoaded', () => {

    // ========================================================
    // 1. SLIDER LOGIC (Only runs if slider exists on the page)
    // ========================================================
    const slidesWrapper = document.querySelector('.slides-wrapper');
    if (slidesWrapper) {
        const slides = document.querySelectorAll('.slide');
        const prevBtn = document.querySelector('.prev-btn');
        const nextBtn = document.querySelector('.next-btn');

        let currentIndex = 0;
        const totalSlides = slides.length;
        let autoSlideInterval;

        const goToSlide = (index) => {
            if (index < 0) currentIndex = totalSlides - 1;
            else if (index >= totalSlides) currentIndex = 0;
            else currentIndex = index;

            const offset = -currentIndex * 100;
            slidesWrapper.style.transform = `translateX(${offset}%)`;
        };

        const nextSlide = () => goToSlide(currentIndex + 1);
        const prevSlide = () => goToSlide(currentIndex - 1);

        const startAutoSlide = () => {
            autoSlideInterval = setInterval(nextSlide, 5000);
        };
        const resetTimer = () => {
            clearInterval(autoSlideInterval);
            startAutoSlide();
        };

        if (nextBtn && prevBtn) {
            nextBtn.addEventListener('click', () => {
                nextSlide();
                resetTimer();
            });
            prevBtn.addEventListener('click', () => {
                prevSlide();
                resetTimer();
            });
        }

        startAutoSlide();
    }

    // ========================================================
    // 2. MODAL (OVERLAY) LOGIC
    // ========================================================
    const modalOverlay = document.getElementById('auth-modal');
    const loginForm = document.getElementById('login-form-container');
    const signupForm = document.getElementById('signup-form-container');

    const closeBtn = document.querySelector('.close-btn');
    const toggleToSignup = document.getElementById('go-to-signup');
    const toggleToLogin = document.getElementById('go-to-login');
    const authTriggers = document.querySelectorAll('.auth-trigger');

    // Helper function to safely open the modal to a specific view
    const openModal = (view = 'login') => {
        if (!modalOverlay) return;
        modalOverlay.style.display = 'flex';
        if (view === 'login') {
            loginForm.style.display = 'block';
            signupForm.style.display = 'none';
        } else {
            signupForm.style.display = 'block';
            loginForm.style.display = 'none';
        }
    };

    // Make navbar links trigger the modal automatically (except links meant to go to real pages)
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        if (link.getAttribute('href') === '#') {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                openModal('login');
            });
        }
    });

    // Attach click events to any explicit elements with the .auth-trigger class
    authTriggers.forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            openModal('login');
        });
    });

    // Close Modal Function
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modalOverlay.style.display = 'none';
        });
    }

    // Close modal if user clicks outside the white content box
    if (modalOverlay) {
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) {
                modalOverlay.style.display = 'none';
            }
        });
    }

    // Toggle between Login and Signup views inside the modal
    if (toggleToSignup) {
        toggleToSignup.addEventListener('click', (e) => {
            e.preventDefault();
            openModal('signup');
        });
    }

    if (toggleToLogin) {
        toggleToLogin.addEventListener('click', (e) => {
            e.preventDefault();
            openModal('login');
        });
    }
});

// Wardrobe Javascript
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
        filterTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                filterTabs.forEach(t => t.classList.remove('active'));
                e.target.classList.add('active');

                activeFilter = e.target.getAttribute('data-category');
                renderWardrobeGrid();
            });
        });
    }
};