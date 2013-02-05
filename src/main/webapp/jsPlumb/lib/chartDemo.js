	;(function() {
		
		window.jsPlumbDemo = {
				
			init : function() {			
	
				var color = "gray";
	
				jsPlumb.importDefaults({
					// notice the 'curviness' argument to this Bezier curve.  the curves on this page are far smoother
					// than the curves on the first demo, which use the default curviness value.			
					Connector : [ "Flowchart" ],
					DragOptions : { cursor: "pointer", zIndex:2000 },
					PaintStyle : { strokeStyle:color, lineWidth:1 },
					EndpointStyle : { radius:0.01, fillStyle:color },
					HoverPaintStyle : {strokeStyle:"#ec9f2e" },
					EndpointHoverStyle : {fillStyle:"#ec9f2e" },			
					Anchors :  [ "TopCenter", "BottomCenter" ]
				});
				
					
				// declare some common values:
				var arrowCommon = { foldback:0.7, fillStyle:color, length:10, width:10 },
					// use three-arg spec to create two different arrows with the common values:
					overlays = [
						[ "Arrow", { location:0.001, direction:-1 }, arrowCommon ]
					];
			
//				jsPlumb.connect({source:"window3", target:"window6", overlays:overlays, detachable:true, reattach:true});
//				jsPlumb.connect({source:"window1", target:"window2", overlays:overlays});
//				jsPlumb.connect({source:"window1", target:"window3", overlays:overlays});
//				jsPlumb.connect({source:"window2", target:"window4", overlays:overlays});
//				jsPlumb.connect({source:"window2", target:"window5", overlays:overlays});
//				jsPlumb.connect({source:"window8", target:"window7", overlays:overlays});
//				jsPlumb.connect({source:"window1", target:"window8", overlays:overlays});
//				jsPlumb.connect({source:"window7", target:"window9", overlays:overlays});
			}
		};
		
	})();
	
	
	
	/*
	 *  This file contains the JS that handles the first init of each jQuery demonstration, and also switching
	 *  between render modes.
	 */
	jsPlumb.bind("ready", function() {
		// chrome fix.
		document.onselectstart = function () { return false; };
		jsPlumb.setRenderMode(jsPlumb.SVG);
		jsPlumbDemo.init();
	});
	

