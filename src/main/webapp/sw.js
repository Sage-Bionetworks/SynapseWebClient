// spin up service worker to install progressive web app, but it does nothing.
this.addEventListener('fetch', function(event) {
  event.respondWith(
    fetch(event.request).catch(function() {
      return caches.match(event.request);
    })
  );
});
console.log('service worker started');