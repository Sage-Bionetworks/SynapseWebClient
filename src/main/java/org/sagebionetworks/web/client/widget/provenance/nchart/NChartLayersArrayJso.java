package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;
import com.google.gwt.core.client.JavaScriptObject;

public class NChartLayersArrayJso extends JavaScriptObject implements NChartLayersArray {

	protected NChartLayersArrayJso() {}

	public final void setLayers(List<NChartLayer> layers) {
		for (int i = 0; i < layers.size(); i++) {
			NChartLayer layer = layers.get(i);
			_setLayer((NChartLayerJso) layer, i);
		}
	}

	private final native void _setLayer(NChartLayerJso layer, int index) /*-{		
																																				this[index] = layer;
																																				}-*/;

}
