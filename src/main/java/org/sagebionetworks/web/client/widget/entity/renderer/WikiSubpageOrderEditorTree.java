package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageNavigationTree.SubpageNavTreeNode;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorViewImpl.TreeItemMovabilityCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTree implements WikiSubpageOrderEditorTreeView.Presenter, SynapseWidgetPresenter {
	
	private WikiSubpageOrderEditorTreeView view;
	
	private Map<V2WikiHeader, SubpageOrderEditorTreeNode> header2node;
	private Map<String, SubpageOrderEditorTreeNode> id2node;
	
	private SubpageOrderEditorTreeNode overallRoot;
	private SubpageOrderEditorTreeNode selectedNode;
	
	private TreeItemMovabilityCallback movabilityCallback;
	
	@Inject
	public WikiSubpageOrderEditorTree(WikiSubpageOrderEditorTreeView view) {
		this.view = view;
		
		header2node = new HashMap<V2WikiHeader, SubpageOrderEditorTreeNode>();
		id2node = new HashMap<String, SubpageOrderEditorTreeNode>();
		
		
		view.setPresenter(this);
	}
	
	public void configure(List<JSONEntity> wikiHeaders, String ownerObjectName) {
		// Make nodes for each header. Populate id2node map and header2node map.
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
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
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
			if (header.getParentId() == null) {
				overallRoot = header2node.get(header);
			} else {
				SubpageOrderEditorTreeNode child = header2node.get(header);
				SubpageOrderEditorTreeNode parent = id2node.get(header.getParentId());
				parent.getChildren().add(child);
			}
		}
		
		view.configure(overallRoot);
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
			movabilityCallback.invoke(selectedCanMoveUp(), selectedCanMoveDown());
		}
	}
	
	private int getSelectedChildIndex() {
		SubpageOrderEditorTreeNode parent = getSelectedParent();
		if (parent == null) {
			return 0;
		}
		return parent.getChildren().indexOf(selectedNode);
		
	}
	
	private boolean selectedCanMoveUp() {
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
	
	public void moveSelectedItem(boolean moveUp) {
		if (moveUp && !selectedCanMoveUp()) {
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
		public V2WikiHeader getHeader()								{		return header;				}
		public List<SubpageOrderEditorTreeNode> getChildren()		{		return children;			}
		public String getText()										{		return this.text;			}
	}
}
