package org.sagebionetworks.web.server.servlet;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.abego.treelayout.Configuration.Location;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.sagebionetworks.web.client.services.LayoutService;
import org.sagebionetworks.web.server.servlet.layout.ProvTreeNodeExtentProvider;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The server-side implementation of the DatasetService. This serverlet will
 * communicate with the platform API via REST.
 * 
 * @author dburdick
 * 
 */
@SuppressWarnings("serial")
public class LayoutServiceImpl extends RemoteServiceServlet implements LayoutService {

	private static Logger logger = Logger.getLogger(LayoutServiceImpl.class
			.getName());

	@Override
	public ProvTreeNode layoutProvTree(ProvTreeNode root) {
		DefaultTreeForTreeLayout<ProvTreeNode> tree = transferTree(root);
		
		// setup the tree layout configuration
		double gapBetweenLevels = 30;
		double gapBetweenNodes = 20;
		DefaultConfiguration<ProvTreeNode> configuration = new DefaultConfiguration<ProvTreeNode>(
				gapBetweenLevels, gapBetweenNodes, Location.Bottom);

		// create the NodeExtentProvider for TextInBox nodes
		ProvTreeNodeExtentProvider nodeExtentProvider = new ProvTreeNodeExtentProvider();

		// create the layout
		TreeLayout<ProvTreeNode> treeLayout = new TreeLayout<ProvTreeNode>(tree, nodeExtentProvider, configuration);

		// fill in position info into original tree
		fillPositions(root, treeLayout.getNodeBounds());
		
		return root;
	}

	/**
	 * Depth first traversal that fills in the x/y position for each node in the tree
	 * @param root
	 * @param nodeBounds
	 */
	private static void fillPositions(ProvTreeNode root, Map<ProvTreeNode, Rectangle2D.Double> nodeBounds) {
		if(root == null || nodeBounds == null) return;

		Rectangle2D.Double bounds = nodeBounds.get(root);
		if(bounds != null) {
			root.setxPos(bounds.getX());
			root.setyPos(bounds.getY());
		}
		Iterator<ProvTreeNode> itr = root.iterator();
		while(itr.hasNext()) {
			ProvTreeNode child = itr.next();
			fillPositions(child, nodeBounds);
		}
	}

	/**
	 * transfer ProvTreeNode structure to TreeLayout tree structure
	 * @param root
	 * @return
	 */
	public static DefaultTreeForTreeLayout<ProvTreeNode> transferTree(ProvTreeNode root) {
		DefaultTreeForTreeLayout<ProvTreeNode> tree = new DefaultTreeForTreeLayout<ProvTreeNode>(root);
		transferTreeProcessNode(tree, root);		
		return tree;
	}
	
	/**
	 * Depth first traversal of mapping connections into the tree
	 * @param tree
	 * @param node
	 */
	private static void transferTreeProcessNode(DefaultTreeForTreeLayout<ProvTreeNode> tree, ProvTreeNode node) {
		if(tree == null || node == null) return;
		Iterator<ProvTreeNode> itr = node.iterator();
		while(itr.hasNext()) {
			ProvTreeNode child = itr.next();
			tree.addChild(node, child);
			transferTreeProcessNode(tree, child);
		}
	}

	
	
	/*
	 * DAG Layout for ProvGraph
	 */	
	@Override
	public ProvGraph dagLayout(ProvGraph provGraph) {
		// convert provGraph into JUNG dag graph
		DirectedGraph<ProvGraphNode, ProvGraphEdge> graph = new DirectedSparseGraph<ProvGraphNode, ProvGraphEdge>();
		Set<ProvGraphEdge> edges = provGraph.getEdges();
		Iterator<ProvGraphEdge> itr = edges.iterator();
		while(itr.hasNext()) {
			ProvGraphEdge edge = itr.next();
			graph.addEdge(edge, edge.getSource(), edge.getSink());
		}
		
		// layout graph and copy values into nodes
		DAGLayout<ProvGraphNode, ProvGraphEdge> dagLayout = new DAGLayout<ProvGraphNode, ProvGraphEdge>(graph);		
		Set<ProvGraphNode> nodes = provGraph.getNodes();
		Iterator<ProvGraphNode> nodeItr = nodes.iterator();
		while(nodeItr.hasNext()) {
			ProvGraphNode node = nodeItr.next();			
			Point2D pt = dagLayout.transform(node);
			node.setxPos(pt.getX());
			node.setyPos(pt.getY());
		}
		return provGraph;
	}

}








