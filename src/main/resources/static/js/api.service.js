/**
 * api.service.js — the ONLY file that knows about URLs and fetch().
 * All mock data has been removed. Every call hits the real Spring Boot backend.
 *
 * Token is persisted in localStorage so it survives page navigations.
 */

const BASE_URL = 'http://localhost:8080';

// ── Token helpers ─────────────────────────────────────────────────────────────
const getToken  = ()  => localStorage.getItem('outfix_token');
const setToken  = (t) => localStorage.setItem('outfix_token', t);
const clearToken = () => localStorage.removeItem('outfix_token');

/** Sends a JSON request with the stored JWT. */
async function request(method, path, body) {
    const opts = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        }
    };
    if (body !== undefined) opts.body = JSON.stringify(body);
    const res  = await fetch(BASE_URL + path, opts);
    if (res.status === 204) return null;
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || `Request failed (${res.status})`);
    return data;
}

/** Sends a multipart/form-data request with the stored JWT. */
async function upload(method, path, formData) {
    const res  = await fetch(BASE_URL + path, {
        method,
        headers: { 'Authorization': `Bearer ${getToken()}` },
        body: formData
    });
    if (res.status === 204) return null;
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || `Upload failed (${res.status})`);
    return data;
}

/** Maps a backend ClothingResponseDto → the shape wardrobe.js expects. */
function toWardrobeItem(c) {
    const tagArray = c.tags ? c.tags.split(',').map(t => t.trim()) : [];
    return {
        id:             c.id,
        name:           tagArray.join(' · ') || 'Clothing item',
        category:       tagArray[0] || '',
        condition:      'N/A',
        tokenFormalitas: c.tokenFormalitas,
        tags:           tagArray,
        imageSrc:       c.clothingImageUrl || ''
    };
}

// ── AuthService ───────────────────────────────────────────────────────────────
const AuthService = {

    /** POST /api/auth/login  { email, password } */
    login: async (email, password) => {
        const res  = await fetch(BASE_URL + '/api/auth/login', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ email, password })
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.message || 'Invalid credentials.');
        setToken(data.token);
        return {
            token: data.token,
            user: {
                id:               data.userId,
                username:         data.username,
                finishedTutorial: data.finishedTutorial
            }
        };
    },

    /** POST /api/auth/register  { username, email, password } */
    register: async (username, email, password) => {
        const res  = await fetch(BASE_URL + '/api/auth/register', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ username, email, password })
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.message || 'Registration failed.');
        setToken(data.token);
        return {
            token: data.token,
            user: {
                id:               data.userId,
                username:         data.username,
                finishedTutorial: data.finishedTutorial
            }
        };
    },

    logout: () => clearToken()
};

// ── UserService ───────────────────────────────────────────────────────────────
const UserService = {

    /** PATCH /api/users/tutorial */
    completeTutorial: async () => request('PATCH', '/api/users/tutorial'),

    /** POST /api/users/face-model  (multipart, field: "file") */
    uploadFaceModel: async (file) => {
        const fd = new FormData();
        fd.append('file', file);
        return upload('POST', '/api/users/face-model', fd);
    }
};

// ── TagService ────────────────────────────────────────────────────────────────
// Real API returns a flat string array. We wrap it into the shape wardrobe.js
// already expects: { categories: [...], conditions: [...] }.
// Conditions are kept static because the backend has no condition concept.
const _CONDITIONS = ['New', 'Like New', 'Good', 'Fair', 'Worn'];

const TagService = {

    /** GET /api/tags → string[] */
    getTags: async () => {
        const tags = await request('GET', '/api/tags');
        return {
            categories: Array.isArray(tags) ? tags : [],
            conditions: _CONDITIONS
        };
    }
};

// ── WardrobeService (clothing items) ─────────────────────────────────────────
// Maps to /api/clothing. Each item is an individual garment, not a saved outfit.
const WardrobeService = {

    /** GET /api/clothing */
    getItems: async () => {
        const items = await request('GET', '/api/clothing');
        return items.map(toWardrobeItem);
    },

    /**
     * POST /api/clothing  (multipart: file?, tokenFormalitas, tags)
     * Accepts the same shape wardrobe.js sends, plus an optional `file` field.
     */
    addItem: async ({ tokenFormalitas, tags, file }) => {
        const fd = new FormData();
        if (file) fd.append('file', file);
        fd.append('tokenFormalitas', tokenFormalitas);
        fd.append('tags', Array.isArray(tags) ? tags.join(',') : tags);
        const item = await upload('POST', '/api/clothing', fd);
        return toWardrobeItem(item);
    },

    /** DELETE /api/clothing/{id} */
    deleteItem: async (id) => request('DELETE', `/api/clothing/${id}`)
};

// ── AgendaService (schedules) ─────────────────────────────────────────────────
// Maps to /api/schedules. Named AgendaService for front-end compatibility.
const AgendaService = {

    /** GET /api/schedules */
    getAgendas: async () => request('GET', '/api/schedules'),

    /** POST /api/schedules  { activityName, targetToken, targetTag, dresscode? } */
    addAgenda: async ({ activityName, targetToken, targetTag, dresscode }) => {
        return request('POST', '/api/schedules', {
            activityName, targetToken, targetTag, dresscode: dresscode || null
        });
    },

    /** DELETE /api/schedules/{id} */
    deleteAgenda: async (id) => request('DELETE', `/api/schedules/${id}`),

    /** GET /api/recommendation/{scheduleId} */
    getRecommendation: async (agendaId) => request('GET', `/api/recommendation/${agendaId}`)
};

// ── GenerateService ───────────────────────────────────────────────────────────
const GenerateService = {

    /**
     * POST /api/mockup/upload  (multipart, field: "image")
     * Returns: { name: "filename.png" }
     */
    uploadImage: async (file) => {
        const fd = new FormData();
        fd.append('image', file);
        return upload('POST', '/api/mockup/upload', fd);
    },

    /**
     * POST /api/mockup/test  { faceFilename, garmentFilename, clothingType }
     * Single-garment virtual try-on.
     */
    submitPrompt: async (faceFilename, garmentFilename, clothingType = 'TOP') => {
        return request('POST', '/api/mockup/test', {
            faceFilename, garmentFilename, clothingType
        });
    },

    /**
     * POST /api/mockup/full  { faceFilename, topFilename, bottomFilename, shoesFilename }
     * Full outfit try-on.
     */
    generateFullOutfit: async (faceFilename, topFilename, bottomFilename, shoesFilename) => {
        return request('POST', '/api/mockup/full', {
            faceFilename, topFilename, bottomFilename, shoesFilename
        });
    }
};
