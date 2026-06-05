/**
 * wardrobe-outfits.js — page controller for wardrobe.html.
 * Shows generated outfit mockups in a table. Polls PENDING rows until they resolve.
 */
document.addEventListener('DOMContentLoaded', () => {

    const tbody     = document.getElementById('outfit-table-body');
    const emptyEl   = document.getElementById('outfit-empty');
    const tableEl   = document.querySelector('.outfit-table');

    let pollTimer = null;

    load();

    async function load() {
        let outfits;
        try {
            outfits = await OutfitService.getItems();
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="outfit-error">${err.message}</td></tr>`;
            return;
        }

        if (!outfits.length) {
            tableEl.style.display = 'none';
            emptyEl.style.display = 'block';
            return;
        }
        tableEl.style.display = '';
        emptyEl.style.display = 'none';

        // newest first
        outfits.sort((a, b) => (b.id || 0) - (a.id || 0));
        tbody.innerHTML = outfits.map(rowHtml).join('');
        wireRowActions();

        // Keep polling while any row is still pending.
        const anyPending = outfits.some(o => o.status === 'PENDING');
        clearTimeout(pollTimer);
        if (anyPending) pollTimer = setTimeout(load, 4000);
    }

    function thumb(clothing) {
        if (clothing && clothing.clothingImageUrl) {
            return `<img class="outfit-thumb" src="${clothing.clothingImageUrl}" alt="garment">`;
        }
        return `<div class="outfit-thumb"></div>`;
    }

    function statusCell(o) {
        if (o.status === 'PENDING') {
            return `<span class="status-pill status-PENDING"><span class="spinner" style="width:12px;height:12px;"></span> Pending…</span>`;
        }
        if (o.status === 'FAILED') {
            return `<span class="status-pill status-FAILED">Server error
                <button class="retry-btn" data-retry="${o.id}">Try again</button></span>`;
        }
        return `<span class="status-pill status-DONE"><i class="fa-solid fa-check"></i> Done</span>`;
    }

    function mockupCell(o) {
        if (o.status === 'DONE' && o.mockupJpgUrl) {
            return `<a href="${o.mockupJpgUrl}" target="_blank"><img class="mockup-thumb" src="${o.mockupJpgUrl}" alt="mockup"></a>`;
        }
        if (o.status === 'FAILED') return `<span style="color:#c0392b;">—</span>`;
        return `<span style="color:#aaa;">Generating…</span>`;
    }

    function rowHtml(o) {
        const event = o.scheduleId ? `Event #${o.scheduleId}` : 'Standalone';
        return `
            <tr data-id="${o.id}">
                <td>${thumb(o.topClothing)}</td>
                <td>${thumb(o.bottomClothing)}</td>
                <td>${event}</td>
                <td>${statusCell(o)}</td>
                <td>${mockupCell(o)}</td>
                <td>
                    <button class="row-fav-btn ${o.favorite ? 'active' : ''}" data-fav="${o.id}" title="Favorite">
                        <i class="fa-solid fa-heart"></i>
                    </button>
                </td>
                <td>
                    <button class="row-del-btn" data-del="${o.id}" title="Delete"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            </tr>`;
    }

    function wireRowActions() {
        tbody.querySelectorAll('[data-fav]').forEach(btn => {
            btn.addEventListener('click', async () => {
                try {
                    const updated = await OutfitService.toggleFavorite(btn.dataset.fav);
                    btn.classList.toggle('active', updated.favorite);
                } catch (err) { alert('Could not update favorite: ' + err.message); }
            });
        });
        tbody.querySelectorAll('[data-del]').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (!confirm('Delete this outfit?')) return;
                try { await OutfitService.deleteItem(btn.dataset.del); load(); }
                catch (err) { alert('Could not delete: ' + err.message); }
            });
        });
        tbody.querySelectorAll('[data-retry]').forEach(btn => {
            btn.addEventListener('click', () => {
                // Regeneration isn't a backend endpoint; send the user to the generate flow.
                window.location.href = '/generate/new';
            });
        });
    }
});
