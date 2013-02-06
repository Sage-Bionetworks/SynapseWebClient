package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class NChartLayersArray extends JavaScriptObject {

	protected NChartLayersArray() { }
	
	public final static NChartLayersArray newInstance(List<NChartLayer> layers) {
		@SuppressWarnings("unchecked")
		JsArray<NChartLayer> layersJs = (JsArray<NChartLayer>) JavaScriptObject.createArray();
		for(NChartLayer layer : layers) {
			layersJs.push(layer);			
		}
		return (NChartLayersArray) ((JavaScriptObject)layersJs);
	}
			
}
