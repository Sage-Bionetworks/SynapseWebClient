package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ActivityLayerNode extends LayerNode {

	private static final int DEFULT_DURATION = 10;
	
	public ActivityLayerNode(ActivityGraphNode activityNode, List<ProvGraphNode> connectedNodes) {
		this.setEvent(activityNode.getId());		
		
		List<String> members = new ArrayList<String>();
		for(ProvGraphNode node : connectedNodes) {
			members.add(node.getId());
		}
		this.setSubnodes(members);
	}
	
	public static JavaScriptObject createActivityLayerNode(ActivityGraphNode activityNode, List<ProvGraphNode> connectedNodes) {		
		String event = activityNode.getId();				
		JsArrayString subnodes = (JsArrayString) JavaScriptObject.createArray();
		for(ProvGraphNode node : connectedNodes) {
			subnodes.push(node.getId());
		}
		return _createActivityLayerNode(event, subnodes, DEFULT_DURATION);
	}
	
	private static native JavaScriptObject _createActivityLayerNode(String event, JsArrayString subnodes, int duration) /*-{
		return { 'duration': duration, 'nodes': [ { 'subnodes': subnodes } ] };
	}-*/;
	
}
