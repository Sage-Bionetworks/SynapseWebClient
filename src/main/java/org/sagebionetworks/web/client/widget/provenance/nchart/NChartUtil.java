package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;

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
	public static NChartCharacters createNChartCharacters(List<ProvGraphNode> graphNodes) {
		NChartCharacters nodes = NChartCharacters.newInstance();
		for(ProvGraphNode node : graphNodes) {
			nodes.addCharacter(node.getId());
		}
		return nodes;
	}
		
	/**
	 * Create an activity node for an NChartLayer
	 * @param activityNode
	 * @param connectedNodes
	 * @return
	 */
	public static NChartLayerNode createActivityLayerNode(ActivityGraphNode activityNode, List<ProvGraphNode> connectedNodes) {				
		List<String> subnodes = new ArrayList<String>();
		for(ProvGraphNode node : connectedNodes) {
			subnodes.add(node.getId());
		}
		return NChartLayerNode.newInstance(activityNode.getId(), subnodes);
	}
	
	/**
	 * Create an entity node for an NChartLayer
	 * @param entityNode
	 * @return
	 */
	public static NChartLayerNode createEntityLayerNode(EntityGraphNode entityNode) {
		List<String> subnodes = new ArrayList<String>();
		subnodes.add(entityNode.getId());
		return NChartLayerNode.newInstance(entityNode.getId(), subnodes);
	}

	
	public final static native LayoutResult layoutGraph(NChartLayersArray layers, NChartCharacters characters) /*-{
    var layers = [
        {'duration': 10,
         'nodes': [
             {'subnodes': ['d1'], 'event':'d1'},
             {'subnodes': ['d2'], 'event':'d2'}
         ]
        },
        {'duration': 10,
         'nodes': [
             {'subnodes': ['d1','d2','d3','d4'], 'event':'A'}
         ]
        },
        {'duration': 10,
         'nodes': [             
            {'subnodes': ['d3'], 'event':'d3'},
            {'subnodes': ['d4'], 'event':'d4'}
         ]
        }
    ];
	        
    var characters = {
        'd1': {},
        'd2': {},
        'd3': {},
        'd4': {},
        'A': {}
    };
        
    var debug = {'features': ['nodes'], 'wireframe': true};
	var conf = {'group_styles': {'pov': {'stroke-width': 3}},
        'debug': debug};	        
	var chart = new $wnd.NChart(characters, layers, conf).calc().plot();
		
	// convert graph into LayoutResult
	var layoutResult = {"someid":[{'x':100, 'y':100}]}; 
	var ncGraph = chart.graph;
	for(var i=0; i<ncGraph.layers.length; i++) {		
		var ncLayer = ncGraph.layers[i];
		for(var j=0; j<ncLayer.nodes.length; j++) {
			var ncNode = ncLayer.nodes[j];
			var provGraphNodeId = ncNode.event;
			var xypoint = { 'x':ncNode.x, 'y':ncNode.y };
			if(!(provGraphNodeId in layoutResult)) { 
				layoutResult[provGraphNodeId] = [];
			}
			layoutResult[provGraphNodeId].push(xypoint);				
		}
	}
	
	return layoutResult;
}-*/;

	public final static native JsArray<XYPoint> _getPointsForId(JavaScriptObject obj, String nodeId) /*-{
		return obj[nodeId]; 
	}-*/;

	
}
