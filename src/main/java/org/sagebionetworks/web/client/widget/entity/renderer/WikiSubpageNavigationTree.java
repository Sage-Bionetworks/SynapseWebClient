package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageNavigationTree implements WikiSubpageNavigationTreeView.Presenter, SynapseWidgetPresenter {

	private WikiSubpageNavigationTreeView view;
	private GlobalApplicationState globalApplicationState;

	private Map<V2WikiHeader, SubpageNavTreeNode> headerToNode;
	private Map<String, SubpageNavTreeNode> idToNode;

	private SubpageNavTreeNode overallRoot;
	CallbackP<WikiPageKey> reloadWikiPageCallback;
	WikiPageKey currentWikiKey;

	@Inject
	public WikiSubpageNavigationTree(WikiSubpageNavigationTreeView view, GlobalApplicationState globalApplicationState) {
		this.globalApplicationState = globalApplicationState;
		this.view = view;
		view.setPresenter(this);
		headerToNode = new HashMap<V2WikiHeader, SubpageNavTreeNode>();
		idToNode = new HashMap<String, SubpageNavTreeNode>();
	}

	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName, Place ownerObjectLink, WikiPageKey currentWikiKey, boolean isEmbeddedInOwnerPage, CallbackP<WikiPageKey> reloadWikiPageCallback) {
		view.clear();
		this.reloadWikiPageCallback = reloadWikiPageCallback;
		this.currentWikiKey = currentWikiKey;
		boolean isCurrentWikiRoot = false;
		// Make nodes for each header. Populate id2node map and header2node map.
		for (V2WikiHeader header : wikiHeaders) {

			Place targetPlace = null;
			String pageTitle;
			if (header.getParentId() == null) {
				targetPlace = ownerObjectLink;
				pageTitle = ownerObjectName;
				isCurrentWikiRoot = currentWikiKey.getWikiPageId() == null || currentWikiKey.getWikiPageId().equals(header.getId());
			} else {
				targetPlace = WikiSubpagesWidget.getLinkPlace(currentWikiKey.getOwnerObjectId(), currentWikiKey.getVersion(), header.getId(), isEmbeddedInOwnerPage);
				if (DisplayUtils.isDefined(header.getTitle())) {
					pageTitle = header.getTitle();
				} else {
					pageTitle = header.getId();
				}

			}
			WikiPageKey wikiPageKey = new WikiPageKey(currentWikiKey.getOwnerObjectId(), currentWikiKey.getOwnerObjectType(), header.getId(), currentWikiKey.getVersion());

			SubpageNavTreeNode node = new SubpageNavTreeNode(pageTitle, targetPlace, wikiPageKey);
			headerToNode.put(header, node);
			idToNode.put(header.getId(), node);
		}

		// Assign child references.
		for (V2WikiHeader header : wikiHeaders) {
			if (header.getParentId() == null) {
				overallRoot = headerToNode.get(header);
			} else {
				SubpageNavTreeNode child = headerToNode.get(header);
				SubpageNavTreeNode parent = idToNode.get(header.getParentId());
				if (parent != null) {
					parent.setCollapsed(isCurrentWikiRoot);
					parent.getChildren().add(child);
				}
			}
		}
		if (overallRoot != null) {
			overallRoot.setCollapsed(false);
			view.configure(overallRoot);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public int getRootChildrenCount() {
		return overallRoot.getChildren().size();
	}

	/*
	 * For testing
	 */
	public SubpageNavTreeNode getOverallRoot() {
		return overallRoot;
	}

	public class SubpageNavTreeNode {
		private List<SubpageNavTreeNode> children;
		private String pageTitle;
		private Place targetPlace;
		private WikiPageKey wikiPageKey;
		private boolean collapsed;

		public SubpageNavTreeNode(String pageTitle, Place targetPlace, WikiPageKey wikiPageKey) {
			this.pageTitle = pageTitle;
			this.targetPlace = targetPlace;
			this.children = new ArrayList<SubpageNavTreeNode>();
			this.wikiPageKey = wikiPageKey;
			collapsed = false;
		}

		/*
		 * Getters
		 */
		public List<SubpageNavTreeNode> getChildren() {
			return this.children;
		}

		public String getPageTitle() {
			return this.pageTitle;
		}

		public Place getTargetPlace() {
			return this.targetPlace;
		}

		public WikiPageKey getWikiPageKey() {
			return this.wikiPageKey;
		}

		public boolean isCollapsed() {
			return collapsed;
		}

		public void setCollapsed(boolean collapsed) {
			this.collapsed = collapsed;
		}
	}

	@Override
	public void reloadWiki(SubpageNavTreeNode node) {
		if (reloadWikiPageCallback != null) {
			reloadWikiPageCallback.invoke(node.getWikiPageKey());
			this.currentWikiKey = node.getWikiPageKey();
			view.resetNavTree(this.overallRoot);
			globalApplicationState.pushCurrentPlace(node.getTargetPlace());
		}
	}

	public boolean contains(String wikiPageKey) {
		return idToNode.containsKey(wikiPageKey);
	}

	public void setPage(String wikiPageKey) {
		if (contains(wikiPageKey)) {
			reloadWiki(idToNode.get(wikiPageKey));
		}
	}

	@Override
	public boolean isCurrentPage(SubpageNavTreeNode root) {
		return root.getWikiPageKey().getWikiPageId().equals(this.currentWikiKey.getWikiPageId());
	}
}
