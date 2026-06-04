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

        const startAutoSlide = () => { autoSlideInterval = setInterval(nextSlide, 5000); };
        const resetTimer = () => { clearInterval(autoSlideInterval); startAutoSlide(); };

        if (nextBtn && prevBtn) {
            nextBtn.addEventListener('click', () => { nextSlide(); resetTimer(); });
            prevBtn.addEventListener('click', () => { prevSlide(); resetTimer(); });
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