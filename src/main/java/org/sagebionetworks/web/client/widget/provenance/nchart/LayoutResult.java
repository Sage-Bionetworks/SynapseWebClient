package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for NGraph LayoutResult
 * ex:  { "d1" : [ {'x':0, 'y':200} ] }
 * @author dburdick
 *
 */
public class LayoutResult extends JavaScriptObject {

	protected LayoutResult() { } 
	
	public final List<XYPoint> getPointsForId(String provGraphNodeId) {
		List<XYPoint> points = new ArrayList<XYPoint>();
		JsArray<XYPoint> pointsJs = _getPointsForId(provGraphNodeId); 
		for(int i=0; i<pointsJs.length(); i++) {
			points.add(pointsJs.get(i));
		}
		return points;
	}
	
	public final native JsArray<XYPoint> _getPointsForId(String nodeId) /*-{
		return this[nodeId]; 
	}-*/;
		
}
