package org.sagebionetworks.web.server.servlet;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.abego.treelayout.Configuration.Location;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.sagebionetworks.web.client.services.LayoutService;
import org.sagebionetworks.web.server.servlet.layout.ProvTreeNodeExtentProvider;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

}
