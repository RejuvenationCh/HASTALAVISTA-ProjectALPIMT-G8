document.addEventListener('DOMContentLoaded', () => {
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
});