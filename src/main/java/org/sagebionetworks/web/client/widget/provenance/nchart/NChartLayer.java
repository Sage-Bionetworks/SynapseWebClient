package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class NChartLayer extends JavaScriptObject {

	protected NChartLayer() { }
	
	public final static NChartLayer newInstance(List<NChartLayerNode> nodes, int duration) {
		@SuppressWarnings("unchecked")
		JsArray<NChartLayerNode> nodesJs = (JsArray<NChartLayerNode>) JavaScriptObject.createArray();
		for(NChartLayerNode node : nodes) {
			nodesJs.push(node);			
		}
		return (NChartLayer) _createLayer(nodesJs, duration);
	}
		
	private final static native JavaScriptObject _createLayer(JsArray<NChartLayerNode> nodes, int duration) /*-{
		return { 'nodes': nodes, 'duration': duration };
	}-*/;
	
}
