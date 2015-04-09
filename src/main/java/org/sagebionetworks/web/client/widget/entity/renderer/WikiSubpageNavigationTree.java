package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
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

	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName, Place ownerObjectLink, WikiPageKey currentWikiKey,
			boolean isEmbeddedInOwnerPage, CallbackP<WikiPageKey> reloadWikiPageCallback) {
		view.clear();
		this.reloadWikiPageCallback = reloadWikiPageCallback;
		this.currentWikiKey = currentWikiKey;

		// Make nodes for each header. Populate id2node map and header2node map.
		for (V2WikiHeader header : wikiHeaders) {

			Place targetPlace = null;
			String pageTitle;
			if (header.getParentId() == null) {
				targetPlace = ownerObjectLink;
				pageTitle = ownerObjectName;
			} else {
				targetPlace = WikiSubpagesWidget.getLinkPlace(currentWikiKey.getOwnerObjectId(), currentWikiKey.getVersion(), header.getId(), isEmbeddedInOwnerPage);
				pageTitle = header.getTitle();
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
				parent.getChildren().add(child);
			}
		}

		view.configure(overallRoot);
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

		public SubpageNavTreeNode(String pageTitle, Place targetPlace, WikiPageKey wikiPageKey) {
			this.pageTitle = pageTitle;
			this.targetPlace = targetPlace;
			this.children = new ArrayList<SubpageNavTreeNode>();
			this.wikiPageKey = wikiPageKey;
		}

		/*
		 * Getters
		 */
		public List<SubpageNavTreeNode> getChildren()       {       return this.children;        }
		public String getPageTitle()                             {       return this.pageTitle;       }
		public Place getTargetPlace()                       {       return this.targetPlace;     }
		public WikiPageKey getWikiPageKey()                 {       return this.wikiPageKey;     }
	}

	@Override
	public void reloadWiki(SubpageNavTreeNode node) {
		if (reloadWikiPageCallback != null) {
			reloadWikiPageCallback.invoke(node.getWikiPageKey());
			globalApplicationState.replaceCurrentPlace(node.getTargetPlace());
			this.currentWikiKey = node.getWikiPageKey();
			view.configure(this.overallRoot);
		}
	}

	@Override
	public boolean isCurrentPage(SubpageNavTreeNode root) {
		return root.getWikiPageKey().getWikiPageId().equals(this.currentWikiKey.getWikiPageId());
	}
}
