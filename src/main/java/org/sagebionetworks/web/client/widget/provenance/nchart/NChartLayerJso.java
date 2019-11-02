package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;
import com.google.gwt.core.client.JavaScriptObject;

public class NChartLayerJso extends JavaScriptObject implements NChartLayer {

	protected NChartLayerJso() {}

	@Override
	public final void setNodes(List<NChartLayerNode> nodes) {
		_clearNodes();
		for (int i = 0; i < nodes.size(); i++) {
			_setNode((NChartLayerNodeJso) nodes.get(i), i);
		}
	}

	private final native void _setNode(NChartLayerNodeJso node, int index) /*-{ 
																																					this.nodes[index] = node;
																																					}-*/;

	@Override
	public final native void setDuration(int duration) /*-{
																											this.duration = duration;
																											}-*/;

	private final native void _clearNodes() /*-{
																					this.nodes = [];
																					}-*/;

}
