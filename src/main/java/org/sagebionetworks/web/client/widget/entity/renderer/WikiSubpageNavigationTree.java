package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
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
	
	public void configure(List<JSONEntity> wikiHeaders, String ownerObjectName, Place ownerObjectLink, WikiPageKey curWikiKey, boolean isEmbeddedInOwnerPage) {
		view.clear();
		
		// Make nodes for each header. Populate id2node map and header2node map.
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
			boolean isCurrentPage = header.getId().equals(curWikiKey.getWikiPageId());
			
			Place targetPlace = null;
			String text;
			if (header.getParentId() == null) {
				targetPlace = ownerObjectLink;
				text = ownerObjectName;
			} else {
				targetPlace = getLinkPlace(curWikiKey.getOwnerObjectId(), curWikiKey.getVersion(), header.getId(), isEmbeddedInOwnerPage);
				text = header.getTitle();
			}
			
			SubpageNavTreeNode node = new SubpageNavTreeNode(header, text, targetPlace, isCurrentPage);
			header2node.put(header, node);
			id2node.put(header.getId(), node);
		}
		
		// Assign child references.
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
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
	
	private Place getLinkPlace(String entityId, Long entityVersion, String wikiId, boolean isEmbeddedInOwnerPage) {
		if (isEmbeddedInOwnerPage)
			return new Synapse(entityId, entityVersion, Synapse.EntityArea.WIKI, wikiId);
		else
			return new Wiki(entityId, ObjectType.ENTITY.toString(), wikiId);
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
		public V2WikiHeader getHeader()						{		return header;				}
		public List<SubpageNavTreeNode> getChildren()		{		return children;			}
		public String getText()								{		return this.text;			}
		public Place getTargetPlace()						{		return targetPlace;			}
		public boolean isCurrentPage()						{		return isCurrentPage;		}
	}
}
