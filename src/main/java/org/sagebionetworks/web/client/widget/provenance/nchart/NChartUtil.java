package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class NChartUtil {

	private static final int DEFULT_DURATION = 10;
	
	/**
	 * Create a list of 'Chatacters' for NChart
	 * @param graphNodes
	 * @return 
	 */
	public static NChartCharacters createNChartCharacters(JsoProvider jsoProvider, List<ProvGraphNode> graphNodes) {
		NChartCharacters characters = jsoProvider.newNChartCharacters();
		for(ProvGraphNode node : graphNodes) {
			characters.addCharacter(node.getId());
		}
		return characters;
	}
		
	/**
	 * Create an activity node for an NChartLayer
	 * @param activityNode
	 * @param connectedNodes
	 * @return
	 */
	public static NChartLayerNode createActivityLayerNode(JsoProvider jsoProvider, ActivityGraphNode activityNode, List<ProvGraphNode> connectedNodes) {				
		List<String> subnodes = new ArrayList<String>();
		for(ProvGraphNode node : connectedNodes) {
			subnodes.add(node.getId());
		}
		NChartLayerNode ln = jsoProvider.newNChartLayerNode();
		ln.setSubnodes(subnodes);
		ln.setEvent(activityNode.getId());
		return ln;
	}
	
	/**
	 * Create an entity node for an NChartLayer
	 * @param entityNode
	 * @return
	 */
	public static NChartLayerNode createEntityLayerNode(JsoProvider jsoProvider, EntityGraphNode entityNode) {
		List<String> subnodes = new ArrayList<String>();
		subnodes.add(entityNode.getId());
		NChartLayerNode ln = jsoProvider.newNChartLayerNode();
		ln.setSubnodes(subnodes);
		ln.setEvent(entityNode.getId());
		return ln;
	}

	public final static native JsArray<XYPointJso> _getPointsForId(JavaScriptObject obj, String nodeId) /*-{
		return obj[nodeId]; 
	}-*/;

	
}
