/**
 * SWC-5131 (Synapse requires two "back" actions to go back to the previous page from the page of a file).
 * Detect Back/Forward navigation, and force a page reload (browser sometimes ignores if it's only a fragment change).
 */


// See https://stackoverflow.com/questions/25806608/how-to-detect-browser-back-button-event-cross-browser
// Investigated using new Navigation API (which does not work for SPA), or popstate (which fires on place change or browser back/forward nav).

document.onmouseover = function() {
	//User's mouse is inside the page.
	window.innerDocClick = true;
}

document.onmouseleave = function() {
	//User's mouse has left the page.
	window.innerDocClick = false;
}

window.onhashchange = function() {
	if (!window.innerDocClick) {
		// hash change invoked by action outside of window (like the back button).
		console.log('detected hash change outside of doc, reloading');
		location.reload();
	} else {
		console.log('detected hash change inside of doc, propagating');
	}
}