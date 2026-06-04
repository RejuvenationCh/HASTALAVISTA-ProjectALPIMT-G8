/**
 * api.service.js — the ONLY file that knows about URLs and fetch().
 *
 * Currently runs on MOCK data with simulated network delays.
 * To connect the real backend: set BASE_URL to your Spring Boot origin
 * and replace each mock Promise with the fetch() call shown in the comment above it.
 * No other file needs to change.
 */

const BASE_URL = 'http://localhost:8080';

// Simulates realistic async latency so loading states are testable.
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// ---------------------------------------------------------------------------
// MOCK DATA STORE
// In-memory arrays that act as a fake database for the session.
// ---------------------------------------------------------------------------
let _mockUsers = [
    { id: 1, username: 'demo@outfix.com', password: 'password123', finishedTutorial: false }
];
let _mockSession = null; // { user, token }

let _mockWardrobeItems = [
    {
        id: 'item_001',
        userId: 1,
        name: 'Napoli Padded Corduroy Zip Up Jacket',
        category: 'Outerwear',
        condition: 'Like New',
        tokenFormalitas: 3,
        tags: ['Men', 'Outerwear'],
        imageSrc: ''
    },
    {
        id: 'item_002',
        userId: 1,
        name: 'High Indigo Yellow Stripe Snapback',
        category: 'Tops',
        condition: 'Good',
        tokenFormalitas: 2,
        tags: ['Unisex', 'Tops'],
        imageSrc: ''
    },
    {
        id: 'item_003',
        userId: 1,
        name: 'Poison Ladies Loose Baggy Joggers',
        category: 'Bottoms',
        condition: 'New',
        tokenFormalitas: 1,
        tags: ['Women', 'Bottoms'],
        imageSrc: ''
    }
];

let _mockAgendas = [
    {
        id: 'agenda_001',
        userId: 1,
        activityName: 'Morning Lecture',
        targetToken: 3,
        targetTag: 'Tops',
        date: '2026-06-10'
    }
];

let _mockTags = {
    categories: ['Tops', 'Bottoms', 'Outerwear', 'Shoes', 'Dresses', 'Activewear'],
    conditions: ['New', 'Like New', 'Good', 'Fair', 'Worn']
};

// ---------------------------------------------------------------------------
// AuthService
// ---------------------------------------------------------------------------
const AuthService = {

    /**
     * REAL: POST /api/auth/login  { username, password }
     * Returns: { token: string, user: { id, username, finishedTutorial } }
     */
    login: async (username, password) => {
        await delay(800);
        const user = _mockUsers.find(u => u.username === username && u.password === password);
        if (!user) throw new Error('Invalid credentials. Try demo@outfix.com / password123');
        const token = 'mock-jwt-token-' + user.id;
        _mockSession = { user, token };
        return { token, user: { id: user.id, username: user.username, finishedTutorial: user.finishedTutorial } };
    },

    /**
     * REAL: POST /api/auth/register  { username, password }
     * Returns: { user: { id, username, finishedTutorial } }
     */
    register: async (username, password) => {
        await delay(1000);
        if (_mockUsers.find(u => u.username === username)) {
            throw new Error('An account with this email already exists.');
        }
        const newUser = { id: Date.now(), username, password, finishedTutorial: false };
        _mockUsers.push(newUser);
        const token = 'mock-jwt-token-' + newUser.id;
        _mockSession = { user: newUser, token };
        return { token, user: { id: newUser.id, username: newUser.username, finishedTutorial: false } };
    },

    logout: () => {
        _mockSession = null;
    }
};

// ---------------------------------------------------------------------------
// UserService
// ---------------------------------------------------------------------------
const UserService = {

    /**
     * REAL: PUT /api/user/tutorial/complete
     * Returns: void
     */
    completeTutorial: async () => {
        await delay(400);
        if (_mockSession) _mockSession.user.finishedTutorial = true;
    }
};

// ---------------------------------------------------------------------------
// TagService
// ---------------------------------------------------------------------------
const TagService = {

    /**
     * REAL: GET /api/tags
     * Returns: { categories: string[], conditions: string[] }
     */
    getTags: async () => {
        await delay(300);
        return { ..._mockTags };
    }
};

// ---------------------------------------------------------------------------
// WardrobeService
// ---------------------------------------------------------------------------
const WardrobeService = {

    /**
     * REAL: GET /api/wardrobe
     * Returns: WardrobeItem[]
     */
    getItems: async () => {
        await delay(700);
        return [..._mockWardrobeItems];
    },

    /**
     * REAL: POST /api/wardrobe  (multipart/form-data)
     * Returns: WardrobeItem
     */
    addItem: async (payload) => {
        await delay(900);
        const newItem = {
            id: 'item_' + Date.now(),
            userId: _mockSession ? _mockSession.user.id : 0,
            name: payload.name,
            category: payload.category,
            condition: payload.condition,
            tokenFormalitas: payload.tokenFormalitas,
            tags: payload.tags || [],
            imageSrc: payload.imageSrc || ''
        };
        _mockWardrobeItems.push(newItem);
        return { ...newItem };
    },

    /**
     * REAL: DELETE /api/wardrobe/:id
     * Returns: void
     */
    deleteItem: async (id) => {
        await delay(400);
        const idx = _mockWardrobeItems.findIndex(i => i.id === id);
        if (idx === -1) throw new Error('Item not found.');
        _mockWardrobeItems.splice(idx, 1);
    }
};

// ---------------------------------------------------------------------------
// AgendaService
// ---------------------------------------------------------------------------
const AgendaService = {

    /**
     * REAL: GET /api/agenda
     * Returns: Agenda[]
     */
    getAgendas: async () => {
        await delay(600);
        return [..._mockAgendas];
    },

    /**
     * REAL: POST /api/agenda  { activityName, targetToken, targetTag, date }
     * Returns: Agenda
     */
    addAgenda: async (payload) => {
        await delay(700);
        const newAgenda = {
            id: 'agenda_' + Date.now(),
            userId: _mockSession ? _mockSession.user.id : 0,
            activityName: payload.activityName,
            targetToken: payload.targetToken,
            targetTag: payload.targetTag,
            date: payload.date
        };
        _mockAgendas.push(newAgenda);
        return { ...newAgenda };
    },

    /**
     * REAL: DELETE /api/agenda/:id
     * Returns: void
     */
    deleteAgenda: async (id) => {
        await delay(400);
        const idx = _mockAgendas.findIndex(a => a.id === id);
        if (idx === -1) throw new Error('Agenda not found.');
        _mockAgendas.splice(idx, 1);
    },

    /**
     * REAL: GET /api/agenda/:id/recommendation
     * Runs the formality-token + tag-intersection query and returns matched wardrobe items.
     * Returns: { status: 'ok'|'empty', items: WardrobeItem[], action?: 'suggest_input' }
     */
    getRecommendation: async (agendaId) => {
        await delay(800);
        const agenda = _mockAgendas.find(a => a.id === agendaId);
        if (!agenda) throw new Error('Agenda not found.');

        const matched = _mockWardrobeItems.filter(item =>
            item.tokenFormalitas === agenda.targetToken &&
            item.tags.includes(agenda.targetTag)
        );

        if (matched.length === 0) {
            return { status: 'empty', items: [], action: 'suggest_input' };
        }
        return { status: 'ok', items: matched };
    }
};

// ---------------------------------------------------------------------------
// GenerateService
// ---------------------------------------------------------------------------
const GenerateService = {

    /**
     * REAL: POST /api/generate/prompt  { faceModelUrl, garmentUrl }
     * Triggers the ComfyUI pipeline via Spring Boot WebClient.
     * Returns: { promptId: string }
     */
    submitPrompt: async (faceModelUrl, garmentUrl) => {
        await delay(1200);
        const promptId = 'prompt_mock_' + Date.now();
        return { promptId };
    },

    /**
     * REAL: GET /api/generate/result/:promptId
     * Polls ComfyUI /history endpoint via Spring Boot proxy.
     * Returns: { status: 'pending'|'complete'|'error', imageUrl?: string }
     */
    pollResult: async (promptId) => {
        // Mock: always resolves as complete after one poll with a placeholder image.
        await delay(2000);
        return {
            status: 'complete',
            imageUrl: 'https://placehold.co/600x800/1a1a1a/ffffff?text=Generated+Outfit'
        };
    }
};
