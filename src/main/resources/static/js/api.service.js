/**
 * api.service.js — the ONLY file that knows about URLs and fetch().
 * Every call hits the real Spring Boot backend. Token is persisted in localStorage.
 */

const BASE_URL = 'http://localhost:8080';

// — Token helpers —
const getToken   = ()  => localStorage.getItem('outfix_token');
const setToken   = (t) => localStorage.setItem('outfix_token', t);
const clearToken = ()  => localStorage.removeItem('outfix_token');

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

/**
 * Item names aren't persisted by the backend (the Clothing entity has no name
 * column), so we keep user-typed names client-side, keyed by the item id.
 */
const ClothingNames = {
    _key: 'outfix_clothing_names',
    _read() {
        try { return JSON.parse(localStorage.getItem(this._key) || '{}'); }
        catch { return {}; }
    },
    _write(map) { localStorage.setItem(this._key, JSON.stringify(map)); },
    get(id)  { return this._read()[id] || null; },
    set(id, name) {
        if (id == null || !name) return;
        const map = this._read();
        map[id] = name;
        this._write(map);
    },
    remove(id) {
        const map = this._read();
        delete map[id];
        this._write(map);
    }
};

/** Maps a backend ClothingResponseDto → the shape the clothing grid expects. */
function toClothingItem(c) {
    const tagArray = c.tags ? c.tags.split(',').map(t => t.trim()) : [];
    return {
        id:              c.id,
        name:            ClothingNames.get(c.id) || tagArray.join(' · ') || 'Clothing item',
        condition:       'N/A',
        tokenFormalitas: c.tokenFormalitas,
        tags:            tagArray,
        imageSrc:        c.clothingImageUrl || '',
        favorite:        !!c.favorite
    };
}

// — AuthService —
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
            user: { id: data.userId, username: data.username, finishedTutorial: data.finishedTutorial }
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
            user: { id: data.userId, username: data.username, finishedTutorial: data.finishedTutorial }
        };
    },

    logout:        () => clearToken(),
    isLoggedIn:    () => !!getToken()
};

// — UserService —
const UserService = {

    /** GET /api/users/me → { id, username, email, faceModelUrl, finishedTutorial } */
    getMe: async () => request('GET', '/api/users/me'),

    /** PATCH /api/users/tutorial */
    completeTutorial: async () => request('PATCH', '/api/users/tutorial'),

    /** POST /api/users/face-model  (multipart, field: "file") */
    uploadFaceModel: async (file) => {
        const fd = new FormData();
        fd.append('file', file);
        return upload('POST', '/api/users/face-model', fd);
    }
};

// — TagService —
// The API returns a flat string array; wrap it into { categories, conditions }.
const _CONDITIONS = ['New', 'Like New', 'Good', 'Fair', 'Worn'];

const TagService = {
    /** GET /api/tags → string[] */
    getTags: async () => {
        const tags = await request('GET', '/api/tags');
        return { categories: Array.isArray(tags) ? tags : [], conditions: _CONDITIONS };
    }
};

// — ClothingService (individual garments → /api/clothing) —
const ClothingService = {

    /** GET /api/clothing */
    getItems: async () => {
        const items = await request('GET', '/api/clothing');
        return items.map(toClothingItem);
    },

    /** POST /api/clothing  (multipart: file?, tokenFormalitas, tags) */
    addItem: async ({ name, tokenFormalitas, tags, file }) => {
        const fd = new FormData();
        if (file) fd.append('file', file);
        fd.append('tokenFormalitas', tokenFormalitas);
        fd.append('tags', Array.isArray(tags) ? tags.join(',') : tags);
        const item = await upload('POST', '/api/clothing', fd);
        ClothingNames.set(item.id, name);   // backend has no name column; keep it client-side
        return toClothingItem(item);
    },

    /** DELETE /api/clothing/{id} */
    deleteItem: async (id) => {
        const res = await request('DELETE', `/api/clothing/${id}`);
        ClothingNames.remove(id);
        return res;
    },

    /** PATCH /api/clothing/{id}/favorite */
    toggleFavorite: async (id) => toClothingItem(await request('PATCH', `/api/clothing/${id}/favorite`))
};

// — MockupService (ComfyUI operations → /api/mockup) —
const MockupService = {

    /**
     * Uploads a file to ComfyUI's input folder for a one-time session use.
     * Returns { name: "comfyui-filename.jpg" }.
     * Does NOT overwrite the user's saved face model.
     */
    uploadTempFace: async (file) => {
        const fd = new FormData();
        fd.append('image', file);
        return upload('POST', '/api/mockup/upload', fd);
    }
};

// — OutfitService (generated outfits → /api/wardrobes) —
const OutfitService = {

    /** GET /api/wardrobes */
    getItems: async () => request('GET', '/api/wardrobes'),

    /** GET /api/wardrobes/{id} */
    getItem: async (id) => request('GET', `/api/wardrobes/${id}`),

    /** POST /api/wardrobes  { scheduleId, topClothingId, bottomClothingId, tempFaceComfyFilename? } */
    create: async ({ scheduleId, topClothingId, bottomClothingId, tempFaceComfyFilename }) =>
        request('POST', '/api/wardrobes', {
            scheduleId, topClothingId, bottomClothingId,
            tempFaceComfyFilename: tempFaceComfyFilename || null
        }),

    /** DELETE /api/wardrobes/{id} */
    deleteItem: async (id) => request('DELETE', `/api/wardrobes/${id}`),

    /** PATCH /api/wardrobes/{id}/favorite */
    toggleFavorite: async (id) => request('PATCH', `/api/wardrobes/${id}/favorite`)
};

// — AgendaService (schedules → /api/schedules) —
const AgendaService = {

    /** GET /api/schedules */
    getAgendas: async () => request('GET', '/api/schedules'),

    /** POST /api/schedules  { activityName, eventDate, targetToken, targetTag, dresscode? } */
    addAgenda: async ({ activityName, eventDate, targetToken, targetTag, dresscode }) =>
        request('POST', '/api/schedules', {
            activityName, eventDate, targetToken, targetTag, dresscode: dresscode || null
        }),

    /** DELETE /api/schedules/{id} */
    deleteAgenda: async (id) => request('DELETE', `/api/schedules/${id}`),

    /** GET /api/recommendation/{scheduleId}/outfit → { status, top, bottom } */
    getRecommendationOutfit: async (scheduleId) =>
        request('GET', `/api/recommendation/${scheduleId}/outfit`),

    /** GET /api/recommendation/{scheduleId}/options → { tops: [...], bottoms: [...] } */
    getRecommendationOptions: async (scheduleId) =>
        request('GET', `/api/recommendation/${scheduleId}/options`)
};

// — NotificationService (client-side, localStorage-backed) —
const NotificationService = {

    _key: 'outfix_notifications',

    _read() {
        try { return JSON.parse(localStorage.getItem(this._key) || '[]'); }
        catch { return []; }
    },
    _write(list) { localStorage.setItem(this._key, JSON.stringify(list)); },

    getAll() { return this._read().sort((a, b) => b.createdAt - a.createdAt); },

    unreadCount() { return this._read().filter(n => !n.read).length; },

    push({ type = 'generation', title, message }) {
        const list = this._read();
        list.push({ id: 'n_' + Date.now(), type, title, message, createdAt: Date.now(), read: false });
        this._write(list);
        document.dispatchEvent(new CustomEvent('outfix:notification'));
    },

    markRead(id) {
        const list = this._read();
        const n = list.find(x => x.id === id);
        if (n) { n.read = true; this._write(list); }
    },

    markAllRead() {
        const list = this._read();
        list.forEach(n => n.read = true);
        this._write(list);
    },

    /** Human-friendly "x min ago" from a ms timestamp. */
    relativeTime(ms) {
        const s = Math.floor((Date.now() - ms) / 1000);
        if (s < 60)    return 'just now';
        if (s < 3600)  return `${Math.floor(s / 60)} min ago`;
        if (s < 86400) return `${Math.floor(s / 3600)} hour(s) ago`;
        return `${Math.floor(s / 86400)} day(s) ago`;
    }
};
