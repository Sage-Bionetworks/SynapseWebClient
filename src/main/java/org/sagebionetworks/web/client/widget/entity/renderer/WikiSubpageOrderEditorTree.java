package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorViewImpl.TreeItemMovabilityCallback;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTree implements WikiSubpageOrderEditorTreeView.Presenter, SynapseWidgetPresenter {
	
	private WikiSubpageOrderEditorTreeView view;
	
	private Map<V2WikiHeader, SubpageOrderEditorTreeNode> header2node;
	private Map<String, SubpageOrderEditorTreeNode> id2node;
	
	private SubpageOrderEditorTreeNode overallRoot;
	private SubpageOrderEditorTreeNode selectedNode;
	
	private TreeItemMovabilityCallback movabilityCallback;
	CallbackP<String> refreshCallback;
	SynapseClientAsync synapseClient;
	
	@Inject
	public WikiSubpageOrderEditorTree(
			WikiSubpageOrderEditorTreeView view,
			SynapseClientAsync synapseClient
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		header2node = new HashMap<V2WikiHeader, SubpageOrderEditorTreeNode>();
		id2node = new HashMap<String, SubpageOrderEditorTreeNode>();
		
		
		view.setPresenter(this);
	}
	
	public void configure(String selectWikiPageId, List<V2WikiHeader> wikiHeaders, String ownerObjectName, CallbackP<String> refreshCallback) {
		this.refreshCallback = refreshCallback;
		view.clear();
		// Make nodes for each header. Populate id2node map and header2node map.
		for (V2WikiHeader header : wikiHeaders) {
			
			String text;
			if (header.getParentId() == null) {
				text = ownerObjectName;
			} else {
				text = header.getTitle();
			}
			
			SubpageOrderEditorTreeNode node = new SubpageOrderEditorTreeNode(header, text);
			header2node.put(header, node);
			id2node.put(header.getId(), node);
		}
		
		// Assign child references.
		for (V2WikiHeader header : wikiHeaders) {
			
			if (header.getParentId() == null) {
				overallRoot = header2node.get(header);
			} else {
				SubpageOrderEditorTreeNode child = header2node.get(header);
				SubpageOrderEditorTreeNode parent = id2node.get(header.getParentId());
				parent.getChildren().add(child);
			}
		}
		
		view.configure(overallRoot);
		
		SubpageOrderEditorTreeNode selectNode = id2node.get(selectWikiPageId);
		if (selectNode != null) {
			selectTreeItem(selectNode);
		}
	}
	
	public List<String> getIdListOrderHint() {
		List<String> idList = new LinkedList<String>();
		addIdsToListRecurse(overallRoot, idList);
		return idList;
	}
	
	private void addIdsToListRecurse(SubpageOrderEditorTreeNode root, List<String> idList) {
		idList.add(root.getHeader().getId());
		for (SubpageOrderEditorTreeNode child : root.getChildren()) {
			addIdsToListRecurse(child, idList);
		}
	}
	
	public void setMovabilityCallback(TreeItemMovabilityCallback movabilityCallback) {
		this.movabilityCallback = movabilityCallback;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void selectTreeItem(SubpageOrderEditorTreeNode node) {
		view.selectTreeItem(node);
		
		selectedNode = node;
		
		if (movabilityCallback != null) {
			movabilityCallback.invoke(selectedCanMoveUpOrRight(), selectedCanMoveDown(), selectedCanMoveLeft());
		}
	}
	
	private int getSelectedChildIndex() {
		SubpageOrderEditorTreeNode parent = getSelectedParent();
		if (parent == null) {
			return 0;
		}
		return parent.getChildren().indexOf(selectedNode);
		
	}
	
	private boolean selectedCanMoveUpOrRight() {
		if (selectedNode == null) return false;
		
		if (selectedNode.getHeader().getParentId() == null) {
			// Overall Root.
			return false;
		}
		
		int childIndex = getSelectedChildIndex();
		if (childIndex <= 0) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean selectedCanMoveLeft() {
		if (selectedNode == null) return false;
		
		if (selectedNode.getHeader().getParentId() == null || getSelectedParent().getHeader().getParentId() == null) {
			// Overall Root.
			return false;
		} else {
			return true;
		}
	}
	
	private boolean selectedCanMoveDown() {
		if (selectedNode == null) return false;
		
		if (selectedNode.getHeader().getParentId() == null) {
			// Overall Root.
			return false;
		}
		
		SubpageOrderEditorTreeNode parent = getSelectedParent();
		int childIndex = getSelectedChildIndex();
		if (childIndex == parent.getChildren().size() - 1 || childIndex < 0) {
			return false;
		} else {
			return true;
		}
	}
	
	//TODO: instead of moving in the tree, update the order (or parent) and refresh.
	public void moveUp() {
		view.moveTreeItem(selectedNode, true);
		
		//TODO: get wiki hint, refresh, and select node
	}
	
	public void moveDown() {
		view.moveTreeItem(selectedNode, false);
		
		//TODO: get wiki hint, refresh, and select node
	}
	
	public void moveLeft() {
		String newParentId = getSelectedParent().getHeader().getParentId();
		V2WikiHeader selectedNodeHeader = selectedNode.getHeader();
		
		// get the v2 wiki page,  set the new parent, and update the v2 wiki page.  then refresh.
		synapseClient.updateWiki
	}
	
	
	
	public void moveSelectedItem(boolean moveUp) {
		if (moveUp && !selectedCanMoveUpOrRight()) {
			throw new IllegalStateException("Selected item cannot be moved up.");
		} else if (!moveUp && !selectedCanMoveDown()) {
			throw new IllegalStateException("Selected item cannot be moved down.");
		}
		
		view.moveTreeItem(selectedNode, moveUp);
		
		int insertIndex;
		if (moveUp) {
			insertIndex = getSelectedChildIndex() - 1;
		} else {
			insertIndex = getSelectedChildIndex() + 1;
		}
		
		SubpageOrderEditorTreeNode parent = getSelectedParent();
		
		parent.getChildren().remove(selectedNode);
		parent.getChildren().add(insertIndex, selectedNode);
		
		selectTreeItem(selectedNode);
	
	}
	
	private SubpageOrderEditorTreeNode getSelectedParent() {
		return id2node.get(selectedNode.getHeader().getParentId());
	}
	
	@Override
	public SubpageOrderEditorTreeNode getSelectedTreeItem() {
		return selectedNode;
	}
	
	@Override
	public SubpageOrderEditorTreeNode getParent(SubpageOrderEditorTreeNode child) {
		if (child.getHeader().getParentId() == null) return null;
		
		return id2node.get(child.getHeader().getParentId());
	}
	
	/*
	 * For Testing
	 */
	public SubpageOrderEditorTreeNode getOverallRoot() {
		return overallRoot;
	}
	
	public class SubpageOrderEditorTreeNode {
		private V2WikiHeader header;
		private List<SubpageOrderEditorTreeNode> children;
		private String text;
		
		public SubpageOrderEditorTreeNode(V2WikiHeader header, String text) {
			this.header = header;
			this.text = text;
			children = new ArrayList<SubpageOrderEditorTreeNode>();
		}
		
		/*
		 * Getters
		 */
		public V2WikiHeader getHeader()                             {       return header;            }
		public List<SubpageOrderEditorTreeNode> getChildren()       {       return children;          }
		public String getText()                                     {       return this.text;         }
	}
}
