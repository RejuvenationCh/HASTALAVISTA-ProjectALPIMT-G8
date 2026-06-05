/**
 * collection.js — page controller for collection.html.
 * Aggregates favorited clothing ("Clothing") and favorited outfits ("Fashion").
 */
document.addEventListener('DOMContentLoaded', () => {

    const grid    = document.getElementById('coll-grid');
    const emptyEl = document.getElementById('coll-empty');
    const tabs    = document.querySelectorAll('.coll-tab');

    let activeFilter = 'all';
    let entries = [];   // unified { kind:'clothing'|'fashion', id, image, label, cat, favorite }

    load();

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            activeFilter = tab.dataset.filter;
            render();
        });
    });

    async function load() {
        try {
            const [clothing, outfits] = await Promise.all([
                ClothingService.getItems(),
                OutfitService.getItems()
            ]);

            const favClothing = clothing
                .filter(c => c.favorite)
                .map(c => ({
                    kind: 'clothing', id: c.id, image: c.imageSrc,
                    label: c.tags.join(' · ') || 'Clothing',
                    cat: `F${c.tokenFormalitas}`, favorite: true
                }));

            const favOutfits = outfits
                .filter(o => o.favorite && o.status === 'DONE')
                .map(o => ({
                    kind: 'fashion', id: o.id, image: o.mockupJpgUrl,
                    label: 'Generated Outfit',
                    cat: o.scheduleId ? `Event #${o.scheduleId}` : 'Outfit', favorite: true
                }));

            entries = [...favOutfits, ...favClothing];
        } catch (err) {
            grid.innerHTML = `<p class="coll-empty">${err.message}</p>`;
            return;
        }
        render();
    }

    function render() {
        const list = entries.filter(e =>
            activeFilter === 'all' ||
            (activeFilter === 'clothing' && e.kind === 'clothing') ||
            (activeFilter === 'fashion'  && e.kind === 'fashion'));

        if (!list.length) {
            grid.innerHTML = '';
            emptyEl.style.display = 'block';
            return;
        }
        emptyEl.style.display = 'none';

        grid.innerHTML = list.map(e => `
            <div class="coll-card" data-kind="${e.kind}" data-id="${e.id}">
                ${e.kind === 'fashion' ? '<span class="badge-fashion">FASHION</span>' : ''}
                <button class="coll-fav" data-kind="${e.kind}" data-id="${e.id}" title="Remove from collection">
                    <i class="fa-solid fa-heart"></i>
                </button>
                ${e.image ? `<img src="${e.image}" alt="${e.label}">` : `<div style="height:230px;background:#f3f3f3;"></div>`}
                <div class="coll-body">
                    <span class="coll-cat">${e.cat}</span>
                    <p style="margin:6px 0 0;">${e.label}</p>
                </div>
            </div>`).join('');

        grid.querySelectorAll('.coll-fav').forEach(btn => {
            btn.addEventListener('click', () => unfavorite(btn.dataset.kind, btn.dataset.id));
        });
    }

    async function unfavorite(kind, id) {
        try {
            if (kind === 'clothing') await ClothingService.toggleFavorite(id);
            else                     await OutfitService.toggleFavorite(id);
            entries = entries.filter(e => !(e.kind === kind && String(e.id) === String(id)));
            render();
        } catch (err) {
            alert('Could not update: ' + err.message);
        }
    }
});
