package org.sagebionetworks.web.client.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class NChartUtil {

	public static final int DEFULT_DURATION = 10;
	public static final double SCALE_X = 1.7; // scale x by
	public static final double SCALE_Y = 0.9; // scale y by
	public static final double LARGE_GRAPH_SCALE_Y = 1.2; // scale y by
	private static final int LARGE_GRAPH_SIZE = 8;

	public static NChartLayersArray createLayers(JsoProvider jsoProvider, ProvGraph graph) {
		Map<ProvGraphNode, Integer> nodeToLayer = new HashMap<ProvGraphNode, Integer>();

		// identify start nodes
		List<ProvGraphNode> startNodes = new ArrayList<ProvGraphNode>();
		for (ProvGraphNode n : graph.getNodes()) {
			if (n.isStartingNode())
				startNodes.add(n);
		}

		calculateNodeToLayer(graph, nodeToLayer, startNodes);

		// build reverse lookup
		int maxLayer = 0;
		Map<Integer, List<ProvGraphNode>> layerToNode = new HashMap<Integer, List<ProvGraphNode>>();
		for (ProvGraphNode node : nodeToLayer.keySet()) {
			int layer = nodeToLayer.get(node);
			if (layer > maxLayer)
				maxLayer = layer;
			if (!layerToNode.containsKey(layer))
				layerToNode.put(layer, new ArrayList<ProvGraphNode>());
			layerToNode.get(layer).add(node);
		}

		// build layers in order
		List<NChartLayer> layers = new ArrayList<NChartLayer>();
		for (int i = 0; i <= maxLayer; i++) {
			List<NChartLayerNode> layerNodes = new ArrayList<NChartLayerNode>();
			if (!layerToNode.containsKey(i))
				continue; // possible with strange shifting in layer creation
			// process ProvGraphNodes into a list of NChartLayerNodes
			for (ProvGraphNode node : layerToNode.get(i)) {
				if (node instanceof ActivityGraphNode) {
					List<ProvGraphNode> connectedNodes = getConnectedNodes(graph.getEdges(), node);
					layerNodes.add(createActivityLayerNode(jsoProvider, (ActivityGraphNode) node, connectedNodes));
				} else if (node instanceof EntityGraphNode) {
					layerNodes.add(createEntityLayerNode(jsoProvider, (EntityGraphNode) node));
				} else if (node instanceof ExpandGraphNode) {
					layerNodes.add(createExpandLayerNode(jsoProvider, (ExpandGraphNode) node));
				} else if (node instanceof ExternalGraphNode) {
					layerNodes.add(createExternalLayerNode(jsoProvider, (ExternalGraphNode) node));
				}
			}
			// build NChartLayer
			NChartLayer layer = jsoProvider.newNChartLayer();
			layer.setDuration(DEFULT_DURATION);
			layer.setNodes(layerNodes);
			layers.add(layer);
		}

		NChartLayersArray layersArray = jsoProvider.newNChartLayersArray();
		layersArray.setLayers(layers);
		return layersArray;
	}

	private static void calculateNodeToLayer(ProvGraph graph, Map<ProvGraphNode, Integer> nodeToLayer, List<ProvGraphNode> startNodes) {
		// depth first traverse graph for each start node. Prioritize deeper placement in layers
		for (ProvGraphNode startNode : startNodes) {
			processLayer(0, startNode, nodeToLayer, graph);
		}

		// 2nd pass over graph to make assure all activities are lower than their used nodes (SWC-580)
		for (ProvGraphNode node : graph.getNodes()) {
			if (node instanceof ActivityGraphNode) {
				Integer activityDepth = nodeToLayer.get(node);
				EdgesForNode nodeEdges = getEdgesForNode(graph.getEdges(), node);
				// check used (outgoing) nodes and if they are lower, reassign this activity node's depth
				for (ProvGraphEdge edge : nodeEdges.out) {
					Integer outDepth = nodeToLayer.get(edge.getSink());
					if (activityDepth >= outDepth) {
						Integer newDepth = outDepth - 1 < 0 ? 0 : outDepth - 1;
						nodeToLayer.put(node, newDepth);
					}
				}
			}
		}
	}

	/**
	 * Create a list of 'Chatacters' for NChart
	 * 
	 * @param graphNodes
	 * @return
	 */
	public static NChartCharacters createNChartCharacters(JsoProvider jsoProvider, Set<ProvGraphNode> graphNodes) {
		NChartCharacters characters = jsoProvider.newNChartCharacters();
		for (ProvGraphNode node : graphNodes) {
			characters.addCharacter(node.getId());
		}
		return characters;
	}

	/**
	 * Create an activity node for an NChartLayer
	 * 
	 * @param activityNode
	 * @param connectedNodes
	 * @return
	 */
	public static NChartLayerNode createActivityLayerNode(JsoProvider jsoProvider, ActivityGraphNode activityNode, List<ProvGraphNode> connectedNodes) {
		List<String> subnodes = new ArrayList<String>();
		for (ProvGraphNode node : connectedNodes) {
			subnodes.add(node.getId());
		}
		NChartLayerNode ln = jsoProvider.newNChartLayerNode();
		ln.setSubnodes(subnodes);
		ln.setEvent(activityNode.getId());
		return ln;
	}

	/**
	 * Create an entity node for an NChartLayer
	 * 
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

	/**
	 * Create an entity node for an NChartLayer
	 * 
	 * @param node
	 * @return
	 */
	public static NChartLayerNode createExpandLayerNode(JsoProvider jsoProvider, ExpandGraphNode node) {
		List<String> subnodes = new ArrayList<String>();
		subnodes.add(node.getId());
		NChartLayerNode ln = jsoProvider.newNChartLayerNode();
		ln.setSubnodes(subnodes);
		ln.setEvent(node.getId());
		return ln;
	}

	/**
	 * Create an external node for an NChartLayer
	 * 
	 * @param entityNode
	 * @return
	 */
	public static NChartLayerNode createExternalLayerNode(JsoProvider jsoProvider, ExternalGraphNode node) {
		List<String> subnodes = new ArrayList<String>();
		subnodes.add(node.getId());
		NChartLayerNode ln = jsoProvider.newNChartLayerNode();
		ln.setSubnodes(subnodes);
		ln.setEvent(node.getId());
		return ln;
	}

	/**
	 * Fills the positions in LayoutResult back into the graph
	 * 
	 * @param layoutResult
	 * @param graph
	 */
	public static void fillPositions(LayoutResult layoutResult, ProvGraph graph) {
		// find min and max Y for mirror transform
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		for (ProvGraphNode node : graph.getNodes()) {
			List<XYPoint> xyPoints = layoutResult.getPointsForId(node.getId());
			if (xyPoints != null && xyPoints.size() > 0) {
				XYPoint pt = xyPoints.get(0);
				if (pt.getX() < minY)
					minY = pt.getX();
				if (pt.getX() > maxY)
					maxY = pt.getX();
			}
		}
		int range = minY + maxY;
		final double scaleY = graph.getNodes().size() >= LARGE_GRAPH_SIZE ? LARGE_GRAPH_SCALE_Y : SCALE_Y;
		for (ProvGraphNode node : graph.getNodes()) {
			List<XYPoint> xyPoints = layoutResult.getPointsForId(node.getId());
			if (xyPoints != null && xyPoints.size() > 0) {
				// swap X and Y, scale X and transpose graph
				XYPoint pt = xyPoints.get(0);
				int x = (int) (pt.getY() * SCALE_X);
				int y = (-1 * pt.getX()) + range; // reflect y axis
				y = new Long(Math.round(y * scaleY)).intValue();
				node.setxPos(x);
				node.setyPos(y);
			}
		}
	}

	public static void repositionExpandNodes(ProvGraph graph) {
		if (graph != null && graph.getEdges() != null) {
			for (ProvGraphEdge edge : graph.getEdges()) {
				if (edge.getSink() instanceof ExpandGraphNode) {
					// place expand node directly above node
					edge.getSink().setxPos(edge.getSource().getxPos() + 32);
					edge.getSink().setyPos(edge.getSource().getyPos() - 35);
				}
			}
		}

	}

	/*
	 * Private Methods
	 */
	private static List<ProvGraphNode> getConnectedNodes(Set<ProvGraphEdge> edges, ProvGraphNode node) {
		List<ProvGraphNode> connected = new ArrayList<ProvGraphNode>();
		EdgesForNode nodeEdges = getEdgesForNode(edges, node);
		for (ProvGraphEdge edge : nodeEdges.in) {
			connected.add(edge.getSource());
		}
		for (ProvGraphEdge edge : nodeEdges.out) {
			connected.add(edge.getSink());
		}
		return connected;
	}

	private static void processLayer(int layer, ProvGraphNode node, Map<ProvGraphNode, Integer> nodeToLayer, ProvGraph graph) {
		// set layer for this node
		setLayerValue(layer, node, nodeToLayer);

		EdgesForNode nodeEdges = getEdgesForNode(graph.getEdges(), node);

		// assign layer for leafs and also maximal layer depth for incoming edges (looking back down)
		for (ProvGraphEdge incomingEdge : nodeEdges.in) {
			setLayerValue(layer - 1, incomingEdge.getSource(), nodeToLayer);
		}

		// process outgoing edges
		for (ProvGraphEdge outgoingEdge : nodeEdges.out) {
			processLayer(layer + 1, outgoingEdge.getSink(), nodeToLayer, graph);
		}

	}

	private static void setLayerValue(int layer, ProvGraphNode node, Map<ProvGraphNode, Integer> nodeToLayer) {
		Integer existingLayer = nodeToLayer.get(node);
		if (existingLayer == null || existingLayer < layer) {
			nodeToLayer.put(node, layer);
		}
	}

	/**
	 * can improve performance
	 * 
	 * @param edges
	 * @param node
	 * @return
	 */
	private static EdgesForNode getEdgesForNode(Set<ProvGraphEdge> edges, ProvGraphNode node) {
		EdgesForNode edgesForNode = new EdgesForNode();
		edgesForNode.in = new ArrayList<ProvGraphEdge>();
		edgesForNode.out = new ArrayList<ProvGraphEdge>();
		for (ProvGraphEdge edge : edges) {
			if (edge.getSource().equals(node))
				edgesForNode.out.add(edge);
			if (edge.getSink().equals(node)) {
				edgesForNode.in.add(edge);
			}
		}
		return edgesForNode;
	}

	private static class EdgesForNode {
		List<ProvGraphEdge> in;
		List<ProvGraphEdge> out;
	}

}
