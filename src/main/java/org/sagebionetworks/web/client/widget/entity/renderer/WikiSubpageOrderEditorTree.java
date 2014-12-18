package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTree implements WikiSubpageOrderEditorTreeView.Presenter, SynapseWidgetPresenter {
	
	private WikiSubpageOrderEditorTreeView view;
	
	private Map<V2WikiHeader, SubpageNode> header2node;
	private Map<String, SubpageNode> id2node;
	
	private SubpageNode overallRoot;
	
	@Inject
	public WikiSubpageOrderEditorTree(WikiSubpageOrderEditorTreeView view) {
		this.view = view;
		
		header2node = new HashMap<V2WikiHeader, SubpageNode>();
		id2node = new HashMap<String, SubpageNode>();
		overallRoot = new SubpageNode(null);
	}
	
	public void buildTree(List<JSONEntity> wikiHeaders) {
		// Make nodes for each header. Populate id2node map and header2node map.
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
			SubpageNode node = new SubpageNode(header);
			header2node.put(header, node);
			id2node.put(header.getId(), node);
		}
		
		// Assign child references.
		for (JSONEntity headerJSONEntity : wikiHeaders) {
			V2WikiHeader header = (V2WikiHeader) headerJSONEntity;
			
			if (header.getParentId() == null) {
				overallRoot = header2node.get(header);
			} else {
				SubpageNode child = header2node.get(header);
				SubpageNode parent = id2node.get(header.getParentId());
				parent.getChildren().add(child);
			}
		}
		
		view.configure(overallRoot);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public class SubpageNode {
		private V2WikiHeader header;
		private List<SubpageNode> children;
		private String text;
		private Place targetPlace;
		private boolean isCurrentPage;
		
		public SubpageNode(V2WikiHeader header) {
			this.header = header;
			children = new ArrayList<SubpageNode>();
		}
		
		/*
		 * Getters and Setters
		 */
		public V2WikiHeader getHeader()						{		return header;				}
		public void setHeader(V2WikiHeader header)			{		this.header = header;		}
		public List<SubpageNode> getChildren()				{		return children;			}
		public void getChildren(List<SubpageNode> children)	{		this.children = children;	}
		
	}
	
}
