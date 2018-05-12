if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
      position = position || 0;
      return this.substr(position, searchString.length) === searchString;
  };
}


/***************************************************
	SWC-4210: detect first time touchstart is detected, and hide all tooltips!
***************************************************/
window.addEventListener('touchstart', function onFirstTouch() {
	jQuery("<style type='text/css'> .tooltip { display: none !important;} </style>").appendTo("head");
	  // stop listening now
	  window.removeEventListener('touchstart', onFirstTouch, false);
	}, false);
