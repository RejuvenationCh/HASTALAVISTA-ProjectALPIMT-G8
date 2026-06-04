/**
 * AppState — single source of truth for all UI state.
 * Components READ from this object and call actions to mutate it.
 * When the backend is live, only api.service.js changes — not this file.
 */
const AppState = {
    auth: {
        isLoggedIn: false,
        user: null,      // { id, username, finishedTutorial }
        token: null
    },
    wardrobe: {
        status: 'IDLE',  // IDLE | PENDING | SUCCESS | ERROR
        items: [],
        activeCategory: 'all',
        activeCondition: 'all',
        error: null
    },
    tags: {
        status: 'IDLE',
        categories: [],
        conditions: [],
        error: null
    },
    agenda: {
        status: 'IDLE',  // IDLE | PENDING | SUCCESS | ERROR
        items: [],
        error: null
    },
    generate: {
        selectedItems: [],       // WardrobeItem[] chosen in the tiny overlay picker
        status: 'IDLE',          // IDLE | PENDING | SUCCESS | ERROR
        promptId: null,          // returned by ComfyUI /prompt
        outputImageUrl: null,
        error: null
    }
};
