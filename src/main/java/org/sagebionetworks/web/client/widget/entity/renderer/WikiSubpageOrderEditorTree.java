package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorViewImpl.TreeItemMovabilityCallback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTree implements WikiSubpageOrderEditorTreeView.Presenter, SynapseWidgetPresenter {

	public static final String MOVE_DOWN_ERROR = "Selected item cannot be moved down.";

	public static final String MOVE_UP_ERROR = "Selected item cannot be moved up.";

	public static final String MOVE_RIGHT_ERROR = "Selected item cannot be moved right.";

	public static final String MOVE_LEFT_ERROR = "Selected item cannot be moved left.";

	private WikiSubpageOrderEditorTreeView view;

	private Map<V2WikiHeader, SubpageOrderEditorTreeNode> header2node;
	private Map<String, SubpageOrderEditorTreeNode> id2node;

	private SubpageOrderEditorTreeNode overallRoot;
	private SubpageOrderEditorTreeNode selectedNode;

	private TreeItemMovabilityCallback movabilityCallback;
	CallbackP<String> refreshCallback;
	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;
	WikiPageKey wikiKey;
	V2WikiOrderHint hint;
	boolean updatingHint = false;

	@Inject
	public WikiSubpageOrderEditorTree(WikiSubpageOrderEditorTreeView view, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		header2node = new HashMap<V2WikiHeader, SubpageOrderEditorTreeNode>();
		id2node = new HashMap<String, SubpageOrderEditorTreeNode>();
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}

	public void configure(String selectWikiPageId, WikiPageKey wikiKey, List<V2WikiHeader> wikiHeaders, String ownerObjectName, V2WikiOrderHint hint, CallbackP<String> refreshCallback) {
		this.refreshCallback = refreshCallback;
		this.wikiKey = wikiKey;
		this.hint = hint;
		updatingHint = false;
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

		view.configure(overallRoot, wikiKey.getOwnerObjectId());

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
		if (selectedNode == null)
			return false;

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
		if (selectedNode == null)
			return false;

		if (selectedNode.getHeader().getParentId() == null || getSelectedParent().getHeader().getParentId() == null) {
			// Overall Root.
			return false;
		} else {
			return true;
		}
	}

	private boolean selectedCanMoveDown() {
		if (selectedNode == null)
			return false;

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

	public void moveUp() {
		moveSelectedItem(true);
	}

	public void moveDown() {
		moveSelectedItem(false);
	}

	private void updateOrderHint() {
		updatingHint = true;
		List<String> newOrderHint = getIdListOrderHint();
		hint.setIdList(newOrderHint);
		synAlert.clear();
		jsClient.updateV2WikiOrderHint(wikiKey, hint, new AsyncCallback<V2WikiOrderHint>() {
			@Override
			public void onSuccess(V2WikiOrderHint newHint) {
				hint = newHint;
				updatingHint = false;
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				updatingHint = false;
			}
		});
	}

	public void moveLeft() {
		synAlert.clear();
		if (!selectedCanMoveLeft()) {
			synAlert.showError(MOVE_LEFT_ERROR);
			return;
		}
		String newParentId = getSelectedParent().getHeader().getParentId();
		updateSelectedParent(newParentId);
	}

	public void moveRight() {
		synAlert.clear();
		if (!selectedCanMoveUpOrRight()) {
			synAlert.showError(MOVE_RIGHT_ERROR);
			return;
		}

		SubpageOrderEditorTreeNode parent = getSelectedParent();
		int selectedChildIndex = getSelectedChildIndex();
		SubpageOrderEditorTreeNode siblingAboveSelected = parent.getChildren().get(selectedChildIndex - 1);
		// siblingAboveSelected is the new parent
		String newParentId = siblingAboveSelected.getHeader().getId();
		updateSelectedParent(newParentId);
	}

	public void updateSelectedParent(final String newParentId) {
		V2WikiHeader selectedNodeHeader = selectedNode.getHeader();
		final WikiPageKey key = new WikiPageKey();
		key.setOwnerObjectId(wikiKey.getOwnerObjectId());
		key.setOwnerObjectType(wikiKey.getOwnerObjectType());
		key.setWikiPageId(selectedNodeHeader.getId());
		// get the v2 wiki page, set the new parent, and update the v2 wiki page. then refresh.
		jsClient.getV2WikiPage(key, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage selectedPage) {
				selectedPage.setParentWikiId(newParentId);
				updateWikiPage(key, selectedPage);
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	public void updateWikiPage(WikiPageKey key, V2WikiPage page) {
		jsClient.updateV2WikiPage(key, page, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage result) {
				refreshCallback.invoke(result.getId());
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	public void moveSelectedItem(boolean moveUp) {
		if (updatingHint) {
			// ignore fast click
			return;
		}
		synAlert.clear();
		if (moveUp && !selectedCanMoveUpOrRight()) {
			synAlert.showError(MOVE_UP_ERROR);
			return;
		} else if (!moveUp && !selectedCanMoveDown()) {
			synAlert.showError(MOVE_DOWN_ERROR);
			return;
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

		updateOrderHint();
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
		if (child.getHeader().getParentId() == null)
			return null;

		return id2node.get(child.getHeader().getParentId());
	}

	public SubpageOrderEditorTreeNode getSubpageOrderEditorTreeNode(V2WikiHeader header) {
		return header2node.get(header);
	}

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
		public V2WikiHeader getHeader() {
			return header;
		}

		public List<SubpageOrderEditorTreeNode> getChildren() {
			return children;
		}

		public String getText() {
			return this.text;
		}
	}
}
