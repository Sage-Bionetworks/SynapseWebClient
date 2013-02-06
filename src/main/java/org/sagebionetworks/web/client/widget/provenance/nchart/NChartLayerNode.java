package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class NChartLayerNode extends JavaScriptObject {

	protected NChartLayerNode() { }
	
	public final static NChartLayerNode newInstance(String event, List<String> subnodes) {			
		JsArrayString subnodesJs = (JsArrayString) JavaScriptObject.createArray();
		for(String subnode : subnodes) {
			subnodesJs.push(subnode);			
		}
		return (NChartLayerNode) _createLayerNode(event, subnodesJs);
	}
	
	private final static native JavaScriptObject _createLayerNode(String event, JsArrayString subnodes) /*-{
		return { 'subnodes': subnodes, 'event': event };
	}-*/;

}
