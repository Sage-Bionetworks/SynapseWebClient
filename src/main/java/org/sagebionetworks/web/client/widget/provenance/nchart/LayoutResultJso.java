package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for NGraph LayoutResult ex: { "d1" : [ {'x':0, 'y':200} ] }
 * 
 * @author dburdick
 *
 */
public class LayoutResultJso extends JavaScriptObject implements LayoutResult {

	protected LayoutResultJso() {}

	@Override
	public final List<XYPoint> getPointsForId(String provGraphNodeId) {
		List<XYPoint> points = new ArrayList<XYPoint>();
		// convert javascript array to XYPoint list
		JsArray<XYPointJso> pointsJs = _getPointsForId(provGraphNodeId);
		if (pointsJs != null) {
			for (int i = 0; i < pointsJs.length(); i++) {
				points.add(pointsJs.get(i));
			}
		}
		return points;
	}

	private final native JsArray<XYPointJso> _getPointsForId(String nodeId) /*-{
																																					return this[nodeId]; 
																																					}-*/;

}
