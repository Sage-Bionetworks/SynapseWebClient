package org.sagebionetworks.web.shared.provenance;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProvGraph implements IsSerializable {

	private Set<ProvGraphNode> nodes;
	private Set<ProvGraphEdge> edges; 

	public ProvGraph() {
		nodes = new HashSet<ProvGraphNode>();
		edges = new HashSet<ProvGraphEdge>();
	}
	
	public ProvGraph(Set<ProvGraphNode> nodes, Set<ProvGraphEdge> edges) {
		this.nodes = nodes;
		this.edges = edges;
	}
	
	public boolean hasNode(ProvGraphNode node) {
		return nodes.contains(node);
	}

	public boolean hasEdge(ProvGraphEdge edge) {
		return edges.contains(edge);
	}
	
	public boolean addNode(ProvGraphNode node) {
		return nodes.add(node);
	}

	public void addEdge(ProvGraphEdge edge) {
		if(!nodes.contains(edge.getSource())) nodes.add(edge.getSource());
		if(!nodes.contains(edge.getSink())) nodes.add(edge.getSink());
		edges.add(edge);
	}
	
	public Set<ProvGraphNode> getNodes() {
		return nodes;
	}

	public Set<ProvGraphEdge> getEdges() {
		return edges;
	}	
	
}
