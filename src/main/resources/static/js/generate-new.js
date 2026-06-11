/**
 * generate-new.js — page controller for generate-new.html.
 * Flow: Face ID check → pick event → recommend top+pants → create mockup → poll → notify.
 */
document.addEventListener('DOMContentLoaded', () => {

    // DOM refs
    const facePrompt   = document.getElementById('face-prompt');
    const faceOk       = document.getElementById('face-ok');
    const faceOkImg    = document.getElementById('face-ok-img');
    const faceFileIn   = document.getElementById('face-file-input');
    const faceUploadBt = document.getElementById('face-upload-btn');
    const faceChangeBtn   = document.getElementById('face-change-btn');
    const faceChangeInput = document.getElementById('face-change-input');
    const faceTempBadge   = document.getElementById('face-temp-badge');
    const faceOkSub       = document.getElementById('face-ok-sub');
    const faceResetBtn    = document.getElementById('face-reset-btn');

    const eventSelect  = document.getElementById('event-select');
    const recommendBt  = document.getElementById('recommend-btn');

    const recoStep     = document.getElementById('reco-step');
    const recoGrid     = document.getElementById('reco-grid');
    const generateBt   = document.getElementById('generate-btn');

    const resultStep   = document.getElementById('result-step');
    const mockupResult = document.getElementById('mockup-result');

    const pickerOverlay  = document.getElementById('picker-overlay');
    const pickerTitle    = document.getElementById('picker-title');
    const pickerGrid     = document.getElementById('picker-grid');
    const pickerEmpty    = document.getElementById('picker-empty');
    const pickerCloseBtn = document.getElementById('picker-close');

    let hasFace   = false;
    let currentReco = null;          // { top, bottom }
    let currentOptions = null;       // { tops: [], bottoms: [] }
    let activePickerSlot = null;     // 'top' | 'bottom'
    let pollTimer = null;
    let tempFaceComfyFilename = null; // set when user picks a one-time face override
    let profileFaceUrl = null;        // saved so we can restore it on reset

    init();

    async function init() {
        await checkFace();
        await loadEvents();
    }

    // — STEP 0: Face ID —
    async function checkFace() {
        try {
            const me = await UserService.getMe();
            if (me && me.faceModelUrl) {
                hasFace = true;
                profileFaceUrl = me.faceModelUrl;
                faceOkImg.src = me.faceModelUrl;
                faceOk.style.display = 'flex';
                facePrompt.style.display = 'none';
            } else {
                hasFace = false;
                facePrompt.style.display = 'flex';
                faceOk.style.display = 'none';
            }
        } catch (err) {
            facePrompt.style.display = 'flex';
        }
        refreshGenerateEnabled();
    }

    // — Temp face: Change button opens the file picker —
    faceChangeBtn.addEventListener('click', () => faceChangeInput.click());

    faceChangeInput.addEventListener('change', async () => {
        const file = faceChangeInput.files[0];
        if (!file) return;

        faceChangeBtn.disabled = true;
        faceChangeBtn.innerHTML = '<span class="spinner"></span> Uploading…';
        try {
            const res = await MockupService.uploadTempFace(file);
            tempFaceComfyFilename = res.name;

            // Preview the new photo locally without hitting the profile endpoint
            faceOkImg.src = URL.createObjectURL(file);
            faceTempBadge.style.display = 'inline-block';
            faceOkSub.textContent = 'Temporary photo — only used for this generation.';
            faceOkSub.style.color = '#b8860b';
            faceResetBtn.style.display = 'inline-flex';
            hasFace = true;
            refreshGenerateEnabled();
        } catch (err) {
            alert('Upload failed: ' + err.message);
        } finally {
            faceChangeBtn.disabled = false;
            faceChangeBtn.innerHTML = '<i class="fa-solid fa-arrows-rotate"></i> Change';
            faceChangeInput.value = '';
        }
    });

    // — Reset: discard the temp face and go back to the saved profile photo —
    faceResetBtn.addEventListener('click', () => {
        tempFaceComfyFilename = null;
        faceOkImg.src = profileFaceUrl;
        faceTempBadge.style.display = 'none';
        faceOkSub.textContent = 'You\'re all set to generate.';
        faceOkSub.style.color = '#888';
        faceResetBtn.style.display = 'none';
    });

    faceUploadBt.addEventListener('click', async () => {
        const file = faceFileIn.files[0];
        if (!file) { alert('Please choose a full-body photo first.'); return; }
        faceUploadBt.disabled = true;
        faceUploadBt.textContent = 'Uploading…';
        try {
            await UserService.uploadFaceModel(file);
            await checkFace();
        } catch (err) {
            alert('Upload failed: ' + err.message);
        } finally {
            faceUploadBt.disabled = false;
            faceUploadBt.textContent = 'Upload';
        }
    });

    // — STEP 1: Events —
    async function loadEvents() {
        try {
            const events = await AgendaService.getAgendas();
            if (!events.length) {
                eventSelect.innerHTML = `<option value="">No events — add one on the Schedule page</option>`;
                return;
            }
            eventSelect.innerHTML = `<option value="">Select an event…</option>` +
                events.map(e => {
                    const date = e.eventDate ? ` (${e.eventDate})` : '';
                    return `<option value="${e.id}">${e.activityName}${date} — F${e.targetToken}</option>`;
                }).join('');
        } catch (err) {
            eventSelect.innerHTML = `<option value="">Failed to load schedule</option>`;
        }
    }

    eventSelect.addEventListener('change', () => {
        recommendBt.disabled = !eventSelect.value;
    });

    // — STEP 2: Recommendation —
    recommendBt.addEventListener('click', async () => {
        const scheduleId = eventSelect.value;
        if (!scheduleId) return;

        recommendBt.disabled = true;
        recommendBt.innerHTML = '<span class="spinner"></span> Finding…';
        try {
            // Fetch the recommendation and all available options in one round-trip
            const [reco, options] = await Promise.all([
                AgendaService.getRecommendationOutfit(scheduleId),
                AgendaService.getRecommendationOptions(scheduleId)
            ]);
            currentReco    = reco;
            currentOptions = options;
            renderReco(reco);
            recoStep.style.display = 'block';
            resultStep.style.display = 'none';
            refreshGenerateEnabled();
        } catch (err) {
            alert('Could not get recommendation: ' + err.message);
        } finally {
            recommendBt.disabled = false;
            recommendBt.innerHTML = '<i class="fa-solid fa-shirt"></i> Get Recommendation';
        }
    });

    function recoCard(label, c, slot) {
        if (!c) {
            return `<div class="reco-missing">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <p>No matching ${label.toLowerCase()} in your clothing.</p>
                        <a href="/clothing">Add one</a>
                    </div>`;
        }
        const tags = c.tags ? c.tags.split(',').map(t => t.trim()).join(' · ') : '';
        return `<div class="reco-card" data-slot="${slot}" title="Click to change">
                    <div class="reco-change-hint"><i class="fa-solid fa-repeat"></i> Change</div>
                    ${c.clothingImageUrl ? `<img class="reco-img" src="${c.clothingImageUrl}" alt="${label}">` : `<div class="reco-img"></div>`}
                    <div class="reco-body">
                        <span class="reco-cat">${label} · F${c.tokenFormalitas}</span>
                        <p style="margin:6px 0 0;">${tags}</p>
                    </div>
                </div>`;
    }

    function renderReco(reco) {
        recoGrid.innerHTML = recoCard('Top', reco.top, 'top') + recoCard('Pants', reco.bottom, 'bottom');
        recoGrid.querySelectorAll('.reco-card[data-slot]').forEach(card => {
            card.addEventListener('click', () => openPicker(card.dataset.slot));
        });
    }

    // — Swap picker —
    function openPicker(slot) {
        activePickerSlot = slot;
        const isTop = slot === 'top';
        pickerTitle.textContent = isTop ? 'Choose a Top' : 'Choose Pants';
        const items = isTop ? currentOptions?.tops : currentOptions?.bottoms;

        if (!items || items.length === 0) {
            pickerGrid.innerHTML = '';
            pickerEmpty.style.display = 'block';
        } else {
            pickerEmpty.style.display = 'none';
            const currentId = isTop ? currentReco?.top?.id : currentReco?.bottom?.id;
            pickerGrid.innerHTML = items.map(item => {
                const tags = item.tags ? item.tags.split(',').map(t => t.trim()).join(' · ') : '';
                const selected = item.id === currentId ? ' selected' : '';
                return `<div class="picker-item${selected}" data-id="${item.id}">
                            <img src="${item.clothingImageUrl || ''}" alt="">
                            <div class="picker-item-body">
                                <div class="picker-cat">F${item.tokenFormalitas}</div>
                                <div class="picker-tags">${tags}</div>
                            </div>
                        </div>`;
            }).join('');
            pickerGrid.querySelectorAll('.picker-item').forEach((el, i) => {
                el.addEventListener('click', () => selectItem(slot, items[i]));
            });
        }

        pickerOverlay.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function closePicker() {
        pickerOverlay.style.display = 'none';
        document.body.style.overflow = '';
        activePickerSlot = null;
    }

    function selectItem(slot, item) {
        if (slot === 'top') currentReco = { ...currentReco, top: item };
        else                currentReco = { ...currentReco, bottom: item };
        closePicker();
        renderReco(currentReco);
        refreshGenerateEnabled();
    }

    pickerCloseBtn.addEventListener('click', closePicker);
    pickerOverlay.addEventListener('click', e => { if (e.target === pickerOverlay) closePicker(); });

    // — STEP 3: Generate + poll —
    function refreshGenerateEnabled() {
        const ready = hasFace && currentReco && currentReco.top && currentReco.bottom;
        if (generateBt) generateBt.disabled = !ready;
        if (generateBt && !hasFace) {
            generateBt.title = 'Add your Face ID first';
        }
    }

    generateBt.addEventListener('click', async () => {
        if (!hasFace) { alert('Please add your Face ID first.'); return; }
        if (!currentReco || !currentReco.top || !currentReco.bottom) {
            alert('Need both a top and pants to generate.'); return;
        }

        generateBt.disabled = true;
        generateBt.innerHTML = '<span class="spinner"></span> Submitting…';

        try {
            const created = await OutfitService.create({
                scheduleId:            Number(eventSelect.value),
                topClothingId:         currentReco.top.id,
                bottomClothingId:      currentReco.bottom.id,
                tempFaceComfyFilename: tempFaceComfyFilename
            });
            resultStep.style.display = 'block';
            mockupResult.innerHTML = `<p class="status-line"><span class="spinner"></span> Generating your mockup… this can take a minute.</p>`;
            pollResult(created.id);
        } catch (err) {
            alert('Could not start generation: ' + err.message);
            generateBt.disabled = false;
            generateBt.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Create Mockup';
        }
    });

    async function pollResult(id) {
        clearTimeout(pollTimer);
        let outfit;
        try {
            outfit = await OutfitService.getItem(id);
        } catch (err) {
            mockupResult.innerHTML = `<p class="err-text">Lost track of the job: ${err.message}</p>`;
            return;
        }

        if (outfit.status === 'PENDING') {
            pollTimer = setTimeout(() => pollResult(id), 4000);
            return;
        }

        generateBt.disabled = false;
        generateBt.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Create Mockup';

        if (outfit.status === 'DONE' && outfit.mockupJpgUrl) {
            mockupResult.innerHTML = `
                <img src="${outfit.mockupJpgUrl}" alt="Your outfit mockup">
                <p class="status-line">Done! Also saved to your <a href="/wardrobe">Wardrobe</a>.</p>`;
            NotificationService.push({
                type: 'generation',
                title: 'Generation Complete',
                message: 'Your outfit mockup is ready to view in your Wardrobe.'
            });
        } else {
            mockupResult.innerHTML = `<p class="err-text">Server error — please try again.</p>`;
            NotificationService.push({
                type: 'generation',
                title: 'Generation Failed',
                message: 'Your outfit mockup could not be generated. Please try again.'
            });
        }
    }
});
