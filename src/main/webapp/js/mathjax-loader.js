window.layoutMath = function(element) {
	var x = ["Typeset",MathJax.Hub,element];
	MathJax.Hub.Queue(x);   
};