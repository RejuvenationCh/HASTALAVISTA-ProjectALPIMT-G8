/**
 * auth-guard.js — included on feature pages only (not index/account).
 * If there's no logged-in session, it shows a blocking popup telling the user to
 * log in first and sends them to the account page, where login lives.
 */
(function () {
    // Logged in → nothing to do.
    if (typeof AuthService !== 'undefined' && AuthService.isLoggedIn()) return;

    function showGate() {
        if (document.getElementById('auth-gate-overlay')) return;

        const overlay = document.createElement('div');
        overlay.id = 'auth-gate-overlay';
        overlay.className = 'auth-gate-overlay';
        overlay.innerHTML = `
            <div class="auth-gate-card" role="dialog" aria-modal="true" aria-labelledby="auth-gate-title">
                <div class="auth-gate-icon"><i class="fa-solid fa-lock"></i></div>
                <h2 id="auth-gate-title">Login required</h2>
                <p>You have to log in first to access the website's features.</p>
                <button class="auth-gate-btn" id="auth-gate-login-btn">Go to Login</button>
            </div>`;

        document.body.appendChild(overlay);
        document.body.style.overflow = 'hidden';   // block scrolling behind the popup

        document.getElementById('auth-gate-login-btn').addEventListener('click', () => {
            window.location.href = '/account';
        });
    }

    if (document.body) showGate();
    else document.addEventListener('DOMContentLoaded', showGate);
})();
