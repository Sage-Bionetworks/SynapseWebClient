// add parser through the tablesorter addParser method 
jQuery.tablesorter.addParser({ 
	// scientific notation
	id: "scinot", 
	is: function(s) { 
	    return /[+\-]?(?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?/.test(s); 
	}, 
	format: function(s) { 
	    return jQuery.tablesorter.formatFloat(s);
	}, 
	type: "numeric" 
});

