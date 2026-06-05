// ── NOTIFICATION MOCK DATA ────────────────────────────────────────────────────
const NOTIF_STORAGE_KEY = 'outfix_notif_read_ids';

const _mockNotifications = [{
        id: 'n1',
        type: 'generation',
        title: 'Generation Complete',
        message: 'Your outfit mockup for "Morning Lecture" is ready to view.',
        time: '2 min ago',
        read: false
    },
    {
        id: 'n2',
        type: 'schedule',
        title: 'Upcoming Event',
        message: "Team Meeting tomorrow — don't forget to plan your outfit.",
        time: '1 hour ago',
        read: false
    },
    {
        id: 'n3',
        type: 'generation',
        title: 'Generation Complete',
        message: 'Your outfit mockup for "Casual Friday" has been generated.',
        time: '3 hours ago',
        read: true
    },
    {
        id: 'n4',
        type: 'schedule',
        title: 'New Agenda Added',
        message: 'Campus Event on June 10 — formality token 3 assigned.',
        time: 'Yesterday',
        read: true
    }
];

document.addEventListener('DOMContentLoaded', () => {

    // ── NOTIFICATION PANEL ────────────────────────────────────────────────────
    const bellBtn = document.getElementById('notif-bell-btn');
    const notifPanel = document.getElementById('notif-panel');
    const notifBadge = document.getElementById('notif-badge');

    if (bellBtn && notifPanel) {

        // Restore read state from previous page visits
        const _savedReadIds = JSON.parse(sessionStorage.getItem(NOTIF_STORAGE_KEY) || '[]');
        _mockNotifications.forEach(n => {
            if (_savedReadIds.includes(n.id)) n.read = true;
        });

        const saveReadState = () => {
            const readIds = _mockNotifications.filter(n => n.read).map(n => n.id);
            sessionStorage.setItem(NOTIF_STORAGE_KEY, JSON.stringify(readIds));
        };

        const renderNotifPanel = () => {
            const unread = _mockNotifications.filter(n => !n.read);
            notifBadge.textContent = unread.length;
            notifBadge.style.display = unread.length > 0 ? 'flex' : 'none';

            if (_mockNotifications.length === 0) {
                notifPanel.innerHTML = `
                    <div class="notif-panel-header">
                        <h3>Notifications</h3>
                    </div>
                    <div class="notif-empty">
                        <i class="fa-regular fa-bell-slash"></i>
                        You're all caught up.
                    </div>`;
                return;
            }

            const items = _mockNotifications.map(n => `
                <div class="notif-item ${n.read ? '' : 'unread'}" data-id="${n.id}">
                    <div class="notif-icon-wrap ${n.type}">
                        <i class="fa-solid ${n.type === 'generation' ? 'fa-wand-magic-sparkles' : 'fa-calendar-check'}"></i>
                    </div>
                    <div class="notif-body">
                        <p class="notif-title">${n.title}</p>
                        <p class="notif-message">${n.message}</p>
                        <span class="notif-time">${n.time}</span>
                    </div>
                    ${!n.read ? '<div class="notif-unread-dot"></div>' : ''}
                </div>`).join('');

            notifPanel.innerHTML = `
                <div class="notif-panel-header">
                    <h3>Notifications</h3>
                    <button class="notif-mark-all-btn" id="mark-all-read-btn">Mark all as read</button>
                </div>
                <div class="notif-list">${items}</div>`;

            // Mark individual as read on click
            notifPanel.querySelectorAll('.notif-item').forEach(el => {
                el.addEventListener('click', () => {
                    const n = _mockNotifications.find(n => n.id === el.dataset.id);
                    if (n) {
                        n.read = true;
                        saveReadState();
                        renderNotifPanel();
                    }
                });
            });

            // Mark all as read
            const markAllBtn = document.getElementById('mark-all-read-btn');
            if (markAllBtn) {
                markAllBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    _mockNotifications.forEach(n => n.read = true);
                    saveReadState();
                    renderNotifPanel();
                });
            }
        };

        renderNotifPanel();

        // Toggle panel open/close
        bellBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            notifPanel.classList.toggle('active');
        });

        // Close when clicking anywhere outside
        document.addEventListener('click', (e) => {
            if (!notifPanel.contains(e.target) && e.target !== bellBtn) {
                notifPanel.classList.remove('active');
            }
        });
    }

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
    // Reads data-modal="signup" to open signup view, defaults to login
    authTriggers.forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            openModal(trigger.dataset.modal || 'login');
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

    // ── LOGIN submit ──────────────────────────────────────────────────────────
    const loginSubmitBtn = document.getElementById('login-submit-btn');
    const loginErrorEl = document.getElementById('login-error');

    if (loginSubmitBtn) {
        loginSubmitBtn.addEventListener('click', async () => {
            const email = document.getElementById('login-email').value.trim();
            const password = document.getElementById('login-password').value.trim();

            if (!email || !password) {
                loginErrorEl.textContent = 'Please enter your email and password.';
                loginErrorEl.style.display = 'block';
                return;
            }

            loginErrorEl.style.display = 'none';
            loginSubmitBtn.textContent = 'Logging in…';
            loginSubmitBtn.disabled = true;

            try {
                const {
                    token,
                    user
                } = await AuthService.login(email, password);
                AppState.auth.isLoggedIn = true;
                AppState.auth.user = user;
                AppState.auth.token = token;

                modalOverlay.style.display = 'none';

                // Redirect to wardrobe after successful login
                window.location.href = '/wardrobe';
            } catch (err) {
                loginErrorEl.textContent = err.message;
                loginErrorEl.style.display = 'block';
                loginSubmitBtn.textContent = 'Login';
                loginSubmitBtn.disabled = false;
            }
        });
    }

    // ── SIGNUP submit ─────────────────────────────────────────────────────────
    const signupSubmitBtn = document.getElementById('signup-submit-btn');
    const signupErrorEl = document.getElementById('signup-error');

    if (signupSubmitBtn) {
        signupSubmitBtn.addEventListener('click', async () => {
            const name = document.getElementById('signup-name').value.trim();
            const email = document.getElementById('signup-email').value.trim();
            const password = document.getElementById('signup-password').value.trim();

            if (!name || !email || !password) {
                signupErrorEl.textContent = 'Please fill in all fields.';
                signupErrorEl.style.display = 'block';
                return;
            }

            signupErrorEl.style.display = 'none';
            signupSubmitBtn.textContent = 'Creating account…';
            signupSubmitBtn.disabled = true;

            try {
                const {
                    token,
                    user
                } = await AuthService.register(email, password);
                AppState.auth.isLoggedIn = true;
                AppState.auth.user = user;
                AppState.auth.token = token;

                modalOverlay.style.display = 'none';

                // New users go to wardrobe — tutorial modal will be handled there later
                window.location.href = '/wardrobe';
            } catch (err) {
                signupErrorEl.textContent = err.message;
                signupErrorEl.style.display = 'block';
                signupSubmitBtn.textContent = 'Create Account';
                signupSubmitBtn.disabled = false;
            }
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
}

/* ============================================================
   OUTFIX — shared interactions
   Drives both generate.html and collection.html.
   Each page is initialised only if its root element exists.
   ============================================================ */
(function () {
  "use strict";

  const $  = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  const IMG = "/image/";

  // Inline garment silhouette used wherever an item has no photo.
  const GARMENT = `<svg class="garment-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 3 4 6l2 3.2 2-1.1V21h8V8.1l2 1.1L20 6l-4.5-3-1.2 1.2a3.3 3.3 0 0 1-4.6 0z"/></svg>`;

  const thumb = (src) => (src ? `<img src="${src}" alt="" loading="lazy">` : GARMENT);

  /* ---------- Toast ---------- */
  let toastTimer;
  function toast(msg) {
    const el = $("#toast");
    if (!el) return;
    el.textContent = msg;
    el.classList.add("is-on");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove("is-on"), 2200);
  }

  /* ---------- Nav + any [data-toast] button ---------- */
  function initNav() {
    $$("[data-toast]").forEach((btn) =>
      btn.addEventListener("click", () => toast(btn.dataset.toast))
    );
  }

  /* ============================================================
     GENERATE PAGE
     ============================================================ */
  const POOLS = {
    tops: [
      { name: "Ruffle Blouse",  src: IMG + "IMAGE (LEFT).jpeg" },
      { name: "Knit Sweater" },
      { name: "Graphic Tee" },
      { name: "Black Turtleneck" },
    ],
    bottoms: [
      { name: "Wide-Leg Jeans" },
      { name: "Pleated Skirt" },
      { name: "Tailored Trousers" },
      { name: "Cargo Pants" },
    ],
    outerwear: [
      { name: "Duffle Coat", src: IMG + "IMAGE (RIGHT).jpeg" },
      { name: "Black Blazer" },
      { name: "Varsity Jacket" },
    ],
    footwear: [
      { name: "Hi-Top Sneakers" },
      { name: "Oxford Loafers" },
      { name: "Chunky Boots" },
    ],
    accessories: [
      { name: "Silk Scarf" },
      { name: "Bucket Hat" },
      { name: "Chain Necklace" },
    ],
  };

  const MOODS = [
    { tag: "Clean & Minimal",  word: "pared-back, monochrome" },
    { tag: "Street Layered",   word: "layered, off-duty street" },
    { tag: "Smart Casual",     word: "sharp but easy smart-casual" },
    { tag: "Soft Romantic",    word: "soft, romantic" },
    { tag: "Bold Statement",   word: "bold, statement-making" },
  ];

  const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];

  function initGenerate(page) {
    const form        = $("#composer", page);
    const input       = $("#composer-input");
    const send        = $("#composer-send");
    const box         = $("#result-box");
    const source      = $("#source");
    const sourceBtn   = $("#source-btn");
    const sourceLabel = $("#source-label");

    /* source dropdown (opens upward) */
    sourceBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      const open = source.classList.toggle("is-open");
      sourceBtn.setAttribute("aria-expanded", String(open));
    });
    $$(".source__menu button", source).forEach((btn) =>
      btn.addEventListener("click", () => {
        $$(".source__menu button", source).forEach((b) =>
          b.setAttribute("aria-checked", "false")
        );
        btn.setAttribute("aria-checked", "true");
        sourceLabel.textContent = btn.dataset.value;
        source.classList.remove("is-open");
        sourceBtn.setAttribute("aria-expanded", "false");
      })
    );
    document.addEventListener("click", () => {
      source.classList.remove("is-open");
      sourceBtn.setAttribute("aria-expanded", "false");
    });

    form.addEventListener("submit", (e) => {
      e.preventDefault();
      generate();
    });

    function buildOutfit(prompt, srcLabel) {
      const mood = pick(MOODS);
      const pieces = [
        { cat: "Top",       ...pick(POOLS.tops) },
        { cat: "Bottom",    ...pick(POOLS.bottoms) },
        { cat: "Outerwear", ...pick(POOLS.outerwear) },
        { cat: "Footwear",  ...pick(POOLS.footwear) },
        { cat: "Accessory", ...pick(POOLS.accessories) },
      ];
      const focus = prompt
        ? `for “${prompt}”`
        : "for an everyday rotation";
      return {
        vibe: {
          tag: mood.tag,
          title: `${pieces[2].name} over ${pieces[0].name}`,
          desc: `A ${mood.word} look ${focus}, pulled from your ${srcLabel}. ` +
                `Five pieces selected to balance proportion, texture and palette — swap any piece or regenerate for a fresh take.`,
        },
        pieces,
      };
    }

    function pieceCard(p) {
      return `<article class="piece">
        <div class="piece__thumb">${thumb(p.src)}</div>
        <div class="piece__meta">
          <div class="piece__cat">${p.cat}</div>
          <div class="piece__name">${p.name}</div>
        </div>
      </article>`;
    }

    function render(o) {
      return `
        <div class="result-head">
          <div class="result-vibe">
            <span class="tag">${o.vibe.tag}</span>
            <h3>${o.vibe.title}</h3>
            <p>${o.vibe.desc}</p>
          </div>
          <div class="result-actions">
            <button class="btn" data-act="regen" type="button">Regenerate</button>
            <button class="btn btn--ghost" data-act="reset" type="button">Start over</button>
          </div>
        </div>
        <div class="result-grid">
          ${o.pieces.map(pieceCard).join("")}
        </div>`;
    }

    function generate() {
      const prompt = input.value.trim();
      const srcLabel = sourceLabel.textContent;
      send.classList.add("is-busy");
      page.dataset.state = "result";
      box.innerHTML =
        `<div style="display:grid;place-items:center;height:100%;color:var(--ink-faint);font-weight:600;">Styling your look…</div>`;

      setTimeout(() => {
        box.innerHTML = render(buildOutfit(prompt, srcLabel));
        send.classList.remove("is-busy");
        $('[data-act="regen"]', box).addEventListener("click", generate);
        $('[data-act="reset"]', box).addEventListener("click", () => {
          page.dataset.state = "default";
          input.value = "";
          box.innerHTML = "";
        });
      }, 550);
    }
  }

  /* ============================================================
     COLLECTION PAGE
     ============================================================ */
  const DATA = {
    collection: [
      { id: "c1",  name: "Ruffle Blouse",     cat: "tops",        src: IMG + "IMAGE (LEFT).jpeg" },
      { id: "c2",  name: "Duffle Coat",        cat: "outerwear",   src: IMG + "IMAGE (RIGHT).jpeg" },
      { id: "c3",  name: "Black Blazer",       cat: "outerwear" },
      { id: "c4",  name: "Wide-Leg Jeans",     cat: "bottoms" },
      { id: "c5",  name: "Knit Sweater",       cat: "tops" },
      { id: "c6",  name: "Pleated Skirt",      cat: "bottoms" },
      { id: "c7",  name: "Bow Mini Dress",     cat: "dresses" },
      { id: "c8",  name: "Tailored Trousers",  cat: "bottoms" },
      { id: "c9",  name: "Oxford Loafers",     cat: "footwear" },
      { id: "c10", name: "Silk Scarf",         cat: "accessories" },
      { id: "c11", name: "Bucket Hat",         cat: "accessories" },
    ],
    market: [
      { id: "m1", name: "Hi-Top Sneakers",  cat: "footwear",    price: 120 },
      { id: "m2", name: "Varsity Jacket",   cat: "outerwear",   price: 98 },
      { id: "m3", name: "Cargo Pants",      cat: "bottoms",     price: 64 },
      { id: "m4", name: "Graphic Tee",      cat: "tops",        price: 34 },
      { id: "m5", name: "Lace Collar Dress",cat: "dresses",     price: 88 },
      { id: "m6", name: "Chain Necklace",   cat: "accessories", price: 28 },
      { id: "m7", name: "Leather Belt",     cat: "accessories", price: 40 },
      { id: "m8", name: "Wool Overcoat",    cat: "outerwear",   price: 150 },
    ],
  };

  function initCollection(page) {
    const grid       = $("#item-grid", page);
    const cats       = $("#cats", page);
    const seg        = $("#source-seg", page);
    const searchEl   = $("#search-input", page);
    const preview    = $("#preview-content", page);
    const undoBtn    = $("#undo-btn", page);
    const redoBtn    = $("#redo-btn", page);
    const uploadEl   = $("#upload-input");

    const owned = DATA.collection.slice(); // working set (grows on upload)
    const state = { source: "collection", cat: "all", q: "", selectedId: null };
    const history = [];
    let hIndex = -1;

    const findItem = (id) =>
      owned.find((i) => i.id === id) || DATA.market.find((i) => i.id === id);

    function currentList() {
      const base = state.source === "market" ? DATA.market : owned;
      return base.filter(
        (it) =>
          (state.cat === "all" || it.cat === state.cat) &&
          (!state.q || it.name.toLowerCase().includes(state.q))
      );
    }

    function cardHTML(it) {
      const price =
        state.source === "market" && it.price
          ? `<span class="card__price">$${it.price}</span>`
          : "";
      return `<button class="card" data-id="${it.id}" type="button">
        <div class="card__thumb">${thumb(it.src)}${price}</div>
        <div class="card__meta">
          <div class="card__name">${it.name}</div>
          <div class="card__cat">${it.cat}</div>
        </div>
      </button>`;
    }

    const addCardHTML = () =>
      `<button class="card card--add" type="button" id="add-card">
        <div class="card__thumb">
          <span class="add-plus">+</span>
          <span style="font-size:12px;font-weight:700;">Add item</span>
        </div>
      </button>`;

    function render() {
      const list = currentList();
      let html = state.source === "collection" ? addCardHTML() : "";
      html += list.map(cardHTML).join("");
      grid.innerHTML = html;

      const addCard = $("#add-card", grid);
      if (addCard) addCard.addEventListener("click", () => uploadEl.click());

      $$(".card[data-id]", grid).forEach((c) =>
        c.addEventListener("click", () => select(c.dataset.id, true))
      );
      if (state.selectedId) {
        const active = grid.querySelector(`.card[data-id="${state.selectedId}"]`);
        if (active) active.classList.add("is-active");
      }
    }

    function renderPreview(it) {
      preview.innerHTML = it
        ? `<div class="stage-card">
             <div class="stage-card__frame">${thumb(it.src)}</div>
             <div class="stage-card__name">${it.name}</div>
             <div class="stage-card__cat">${it.cat}</div>
           </div>`
        : `<div class="stage-empty">${GARMENT}<p>Select an item to preview</p></div>`;
    }

    function updateHistoryButtons() {
      undoBtn.disabled = hIndex <= 0;
      redoBtn.disabled = hIndex >= history.length - 1;
    }

    function select(id, push) {
      state.selectedId = id;
      $$(".card", grid).forEach((c) => c.classList.remove("is-active"));
      const el = grid.querySelector(`.card[data-id="${id}"]`);
      if (el) el.classList.add("is-active");
      renderPreview(findItem(id));
      if (push) {
        history.splice(hIndex + 1);
        history.push(id);
        hIndex = history.length - 1;
        updateHistoryButtons();
      }
    }

    /* category tabs */
    cats.addEventListener("click", (e) => {
      const b = e.target.closest(".cats__btn");
      if (!b) return;
      $$(".cats__btn", cats).forEach((x) => x.classList.remove("is-active"));
      b.classList.add("is-active");
      state.cat = b.dataset.cat;
      render();
    });

    /* Collection / Marketplace toggle */
    seg.addEventListener("click", (e) => {
      const b = e.target.closest(".seg__btn");
      if (!b) return;
      $$(".seg__btn", seg).forEach((x) => x.classList.remove("is-active"));
      b.classList.add("is-active");
      state.source = b.dataset.source;
      render();
    });

    /* search */
    searchEl.addEventListener("input", () => {
      state.q = searchEl.value.trim().toLowerCase();
      render();
    });

    /* upload a new item */
    uploadEl.addEventListener("change", () => {
      const file = uploadEl.files && uploadEl.files[0];
      if (!file) return;
      const item = {
        id: "u" + Date.now(),
        name: file.name.replace(/\.[^.]+$/, "") || "New item",
        cat: state.cat === "all" ? "tops" : state.cat,
        src: URL.createObjectURL(file),
      };
      owned.unshift(item);
      uploadEl.value = "";
      render();
      select(item.id, true);
      toast("Item added to your collection");
    });

    /* undo / redo through the selection history */
    undoBtn.addEventListener("click", () => {
      if (hIndex > 0) { hIndex--; select(history[hIndex], false); updateHistoryButtons(); }
    });
    redoBtn.addEventListener("click", () => {
      if (hIndex < history.length - 1) { hIndex++; select(history[hIndex], false); updateHistoryButtons(); }
    });

    /* footer actions */
    $("#add-to-outfit", page).addEventListener("click", () => {
      if (!state.selectedId) return toast("Pick an item first");
      toast(`Added “${findItem(state.selectedId).name}” to today's outfit`);
    });

    /* boot */
    render();
    renderPreview(null);
    updateHistoryButtons();
  }

  /* ---------- Boot ---------- */
  document.addEventListener("DOMContentLoaded", () => {
    initNav();
    const gen = $("#generate-page");
    const col = $("#collection-page");
    if (gen) initGenerate(gen);
    if (col) initCollection(col);
  });
})();