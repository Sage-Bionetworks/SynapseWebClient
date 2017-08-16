// spin up service worker to install progressive web app, but it does nothing.
this.addEventListener('fetch', function(event) {
  event.respondWith(
    console.log('service worker responds with cached data.');
  );
});
console.log('service worker started');