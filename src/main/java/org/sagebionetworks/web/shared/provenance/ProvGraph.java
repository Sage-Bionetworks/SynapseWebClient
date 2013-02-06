package org.sagebionetworks.web.shared.provenance;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProvGraph implements IsSerializable {

	/**
	 * I would like to use Set and HashSet for nodes and edges but GWT Serialization does not support it
	 * http://stackoverflow.com/questions/13965155/gwt-com-google-gwt-user-client-rpc-serializationexception-for-type-java-util-h
	 * 
	 * So using Map and HashMap instead with a notdefined Boolean value
	 */
	private Map<ProvGraphNode,Void> nodes;
	private Map<ProvGraphEdge,Void> edges; 

	public ProvGraph() {
		nodes = new HashMap<ProvGraphNode,Void>();
		edges = new HashMap<ProvGraphEdge,Void>();
	}
	
	public ProvGraph(Map<ProvGraphNode,Void> nodes, Map<ProvGraphEdge,Void> edges) {
		this.nodes = nodes;
		this.edges = edges;
	}
	
	public boolean hasNode(ProvGraphNode node) {
		return nodes.containsKey(node);
	}

	public boolean hasEdge(ProvGraphEdge edge) {
		return edges.containsKey(edge);
	}
	
	public void addNode(ProvGraphNode node) {
		nodes.put(node, null);
	}

	public void addEdge(ProvGraphEdge edge) {
		if(!nodes.containsKey(edge.getSource())) nodes.put(edge.getSource(), null);
		if(!nodes.containsKey(edge.getSink())) nodes.put(edge.getSink(), null);
		edges.put(edge, null);
	}
	
	public Map<ProvGraphNode, Void> getNodes() {
		return nodes;
	}

	public Map<ProvGraphEdge, Void> getEdges() {
		return edges;
	}	
	
}
