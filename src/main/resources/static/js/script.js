// Notifications are stored client-side by NotificationService (api.service.js).

document.addEventListener('DOMContentLoaded', () => {

    // — Notification panel —
    const bellBtn = document.getElementById('notif-bell-btn');
    const notifPanel = document.getElementById('notif-panel');
    const notifBadge = document.getElementById('notif-badge');

    if (bellBtn && notifPanel) {

        const renderNotifPanel = () => {
            const list   = NotificationService.getAll();
            const unread = list.filter(n => !n.read).length;
            notifBadge.textContent = unread;
            notifBadge.style.display = unread > 0 ? 'flex' : 'none';

            if (list.length === 0) {
                notifPanel.innerHTML = `
                    <div class="notif-panel-header"><h3>Notifications</h3></div>
                    <div class="notif-empty">
                        <i class="fa-regular fa-bell-slash"></i>
                        You're all caught up.
                    </div>`;
                return;
            }

            const items = list.map(n => `
                <div class="notif-item ${n.read ? '' : 'unread'}" data-id="${n.id}">
                    <div class="notif-icon-wrap ${n.type}">
                        <i class="fa-solid ${n.type === 'generation' ? 'fa-wand-magic-sparkles' : 'fa-calendar-check'}"></i>
                    </div>
                    <div class="notif-body">
                        <p class="notif-title">${n.title}</p>
                        <p class="notif-message">${n.message}</p>
                        <span class="notif-time">${NotificationService.relativeTime(n.createdAt)}</span>
                    </div>
                    ${!n.read ? '<div class="notif-unread-dot"></div>' : ''}
                </div>`).join('');

            notifPanel.innerHTML = `
                <div class="notif-panel-header">
                    <h3>Notifications</h3>
                    <button class="notif-mark-all-btn" id="mark-all-read-btn">Mark all as read</button>
                </div>
                <div class="notif-list">${items}</div>`;

            notifPanel.querySelectorAll('.notif-item').forEach(el => {
                el.addEventListener('click', () => {
                    NotificationService.markRead(el.dataset.id);
                    renderNotifPanel();
                });
            });

            const markAllBtn = document.getElementById('mark-all-read-btn');
            if (markAllBtn) {
                markAllBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    NotificationService.markAllRead();
                    renderNotifPanel();
                });
            }
        };

        renderNotifPanel();
        // Re-render whenever any page pushes a notification (e.g. generation done).
        document.addEventListener('outfix:notification', renderNotifPanel);

        bellBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            notifPanel.classList.toggle('active');
        });

        document.addEventListener('click', (e) => {
            if (!notifPanel.contains(e.target) && e.target !== bellBtn) {
                notifPanel.classList.remove('active');
            }
        });
    }

    // — Hamburger menu toggle —
    const hamburgerBtn = document.getElementById('hamburger-btn');
    const navMenu = document.getElementById('nav-links');

    if (hamburgerBtn && navMenu) {
        hamburgerBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            hamburgerBtn.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Close menu when a link is clicked
        navMenu.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                hamburgerBtn.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });

        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!hamburgerBtn.contains(e.target) && !navMenu.contains(e.target)) {
                hamburgerBtn.classList.remove('active');
                navMenu.classList.remove('active');
            }
        });
    }

    // — Slider —

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

    // — Auth modal —
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

    // Open the auth modal from any element with the .auth-trigger class.
    // Reads data-modal="signup" to open the signup view, defaults to login.
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

    // — Login submit —
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
            loginSubmitBtn.textContent = 'Logging in...';
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

    // — Signup submit —
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
            signupSubmitBtn.textContent = 'Creating account...';
            signupSubmitBtn.disabled = true;

            try {
                const {
                    token,
                    user
                } = await AuthService.register(name, email, password);
                AppState.auth.isLoggedIn = true;
                AppState.auth.user = user;
                AppState.auth.token = token;

                modalOverlay.style.display = 'none';

                // New users go to wardrobe; tutorial modal handled there later
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
