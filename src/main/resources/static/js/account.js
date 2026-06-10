/**
 * account.js — page controller for account.html.
 * When the user is logged in, the top card shows their account info (instead of
 * Login/Signup) and "My Mix" lists the outfits they generated (GET /api/wardrobes).
 * Logged out, the page keeps its default Login/Signup + empty-state markup.
 */
document.addEventListener('DOMContentLoaded', () => {

    const authCard = document.getElementById('account-auth-card');
    const mixTitle = document.getElementById('mix-title');
    const mixBody  = document.getElementById('mix-body');

    initAccount();

    async function initAccount() {
        // Token persists in localStorage even though AppState resets each load.
        if (!AuthService.isLoggedIn()) return;   // keep the logged-out markup

        let user;
        try {
            user = await UserService.getMe();
        } catch (err) {
            // Token missing/expired — stay in the logged-out view.
            return;
        }

        AppState.auth.isLoggedIn = true;
        AppState.auth.user = user;
        renderAccountInfo(user);
        loadMix();
    }

    // — Account info (replaces the Login/Signup card) —
    function renderAccountInfo(user) {
        const name  = user.username || 'Your Account';
        const email = user.email || '';
        authCard.innerHTML = `
            <div class="account-card-header">
                <div class="account-user">
                    <div class="account-avatar"><i class="fa-regular fa-user"></i></div>
                    <div class="account-card-text">
                        <h3>${escapeHtml(name)}</h3>
                        <p>${escapeHtml(email)}</p>
                    </div>
                </div>
                <div class="account-actions">
                    <button class="btn-outline" id="account-logout-btn">Logout</button>
                </div>
            </div>`;

        document.getElementById('account-logout-btn').addEventListener('click', () => {
            AuthService.logout();
            window.location.reload();
        });
    }

    // — My Mix (generated outfits) —
    async function loadMix() {
        let outfits;
        try {
            outfits = await OutfitService.getItems();
        } catch (err) {
            mixBody.innerHTML = `
                <div class="mix-empty-state">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <p><strong>Couldn't load your mixes</strong></p>
                    <p>${escapeHtml(err.message)}</p>
                </div>`;
            return;
        }

        outfits = Array.isArray(outfits) ? outfits : [];
        mixTitle.textContent = `My Mix (${outfits.length})`;

        if (!outfits.length) {
            mixBody.innerHTML = `
                <div class="mix-empty-state">
                    <i class="fa-solid fa-box-archive"></i>
                    <p><strong>No Mixing found</strong></p>
                    <p>Mix a Wardrobe to see it listed here.</p>
                </div>`;
            return;
        }

        outfits.sort((a, b) => (b.id || 0) - (a.id || 0));   // newest first
        mixBody.innerHTML = `<div class="mix-grid">${outfits.map(mixCard).join('')}</div>`;
    }

    function mixCard(o) {
        const img = (o.status === 'DONE' && o.mockupJpgUrl)
            ? `<img src="${o.mockupJpgUrl}" alt="Outfit mockup">`
            : (o.topClothing && o.topClothing.clothingImageUrl)
                ? `<img src="${o.topClothing.clothingImageUrl}" alt="Outfit top">`
                : `<div class="mix-card-placeholder"><i class="fa-regular fa-image"></i></div>`;

        const label = o.status === 'DONE' ? 'Done'
                    : o.status === 'FAILED' ? 'Failed'
                    : 'Generating…';
        const event = o.scheduleId ? `Event #${o.scheduleId}` : 'Standalone';

        return `
            <div class="mix-card">
                <div class="mix-card-img">${img}</div>
                <div class="mix-card-body">
                    <span class="mix-card-status status-${o.status || 'PENDING'}">${label}</span>
                    <span class="mix-card-event">${escapeHtml(event)}</span>
                </div>
            </div>`;
    }

    // — Helpers —
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str == null ? '' : String(str);
        return div.innerHTML;
    }
});
