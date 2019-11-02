package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.List;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class NChartLayerNodeJso extends JavaScriptObject implements NChartLayerNode {

	protected NChartLayerNodeJso() {}

	@Override
	public final void setSubnodes(List<String> subnodes) {
		JsArrayString subnodesJs = (JsArrayString) JavaScriptObject.createArray();
		for (String subnode : subnodes) {
			subnodesJs.push(subnode);
		}
		_setSubnodes(subnodesJs);
	}

	private final native void _setSubnodes(JsArrayString subnodes) /*-{ this.subnodes = subnodes; }-*/;

	@Override
	public final native void setEvent(String event) /*-{ this.event = event; }-*/;


}
