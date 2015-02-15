package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageNavigationTree implements WikiSubpageNavigationTreeView.Presenter, SynapseWidgetPresenter {
	
	private WikiSubpageNavigationTreeView view;
	
	private Map<V2WikiHeader, SubpageNavTreeNode> header2node;
	private Map<String, SubpageNavTreeNode> id2node;
	
	private SubpageNavTreeNode overallRoot;
	
	@Inject
	public WikiSubpageNavigationTree(WikiSubpageNavigationTreeView view) {
		this.view = view;
		
		header2node = new HashMap<V2WikiHeader, SubpageNavTreeNode>();
		id2node = new HashMap<String, SubpageNavTreeNode>();
	}
	
	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName, Place ownerObjectLink, WikiPageKey curWikiKey, boolean isEmbeddedInOwnerPage) {
		view.clear();
		
		// Make nodes for each header. Populate id2node map and header2node map.
		for (V2WikiHeader header : wikiHeaders) {
			
			boolean isCurrentPage = header.getId().equals(curWikiKey.getWikiPageId());
			
			Place targetPlace = null;
			String text;
			if (header.getParentId() == null) {
				targetPlace = ownerObjectLink;
				text = ownerObjectName;
			} else {
				targetPlace = WikiSubpagesWidget.getLinkPlace(curWikiKey.getOwnerObjectId(), curWikiKey.getVersion(),
											header.getId(), isEmbeddedInOwnerPage);
				text = header.getTitle();
			}
			
			SubpageNavTreeNode node = new SubpageNavTreeNode(header, text, targetPlace, isCurrentPage);
			header2node.put(header, node);
			id2node.put(header.getId(), node);
		}
		
		// Assign child references.
		for (V2WikiHeader header : wikiHeaders) {
			
			if (header.getParentId() == null) {
				overallRoot = header2node.get(header);
			} else {
				SubpageNavTreeNode child = header2node.get(header);
				SubpageNavTreeNode parent = id2node.get(header.getParentId());
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
		private V2WikiHeader header;
		private List<SubpageNavTreeNode> children;
		private String text;
		private Place targetPlace;
		private boolean isCurrentPage;
		
		public SubpageNavTreeNode(V2WikiHeader header, String text, Place targetPlace, boolean isCurrentPage) {
			this.header = header;
			this.text = text;
			this.targetPlace = targetPlace;
			this.isCurrentPage = isCurrentPage;
			children = new ArrayList<SubpageNavTreeNode>();
		}
		
		/*
		 * Getters
		 */
		public V2WikiHeader getHeader()                     {       return header;          }
		public List<SubpageNavTreeNode> getChildren()       {       return children;        }
		public String getText()                             {       return text;            }
		public Place getTargetPlace()                       {       return targetPlace;	    }
		public boolean isCurrentPage()                      {       return isCurrentPage;   }
	}
}
