const CACHE_NAME = 'visionaid-v1';
const ASSETS_TO_CACHE = [
    '/mobile',
    '/static/icon-192.png',
    '/static/icon-512.png'
];

// Install: cache essential assets
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS_TO_CACHE))
    );
    self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
        )
    );
    self.clients.claim();
});

// Fetch: network-first strategy (API calls always go to network, static assets can fallback to cache)
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);
    
    // Never cache API calls
    if (url.pathname.startsWith('/upload-image') || 
        url.pathname.startsWith('/process-text') || 
        url.pathname.startsWith('/translate') ||
        url.pathname.startsWith('/audio/')) {
        return;
    }

    event.respondWith(
        fetch(event.request)
            .then(response => {
                // Cache successful responses
                const clone = response.clone();
                caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
                return response;
            })
            .catch(() => caches.match(event.request))
    );
});
