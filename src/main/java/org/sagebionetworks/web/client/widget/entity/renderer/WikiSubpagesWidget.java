package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesWidget implements WikiSubpagesView.Presenter, WidgetRendererPresenter {
	
	private WikiSubpagesView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private WikiPageKey wikiKey; 
	private String ownerObjectName, ownerObjectLink;
	
	@Inject
	public WikiSubpagesWidget(WikiSubpagesView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, AdapterFactory adapterFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		
		view.setPresenter(this);
	}	
	
	@Override
	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		this.wikiKey = wikiKey;
		view.clear();
		//figure out owner object name/link
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			//lookup the entity name based on the id
			Reference ref = new Reference();
			ref.setTargetId(wikiKey.getOwnerObjectId());
			List<Reference> allRefs = new ArrayList<Reference>();
			allRefs.add(ref);
			ReferenceList list = new ReferenceList();
			list.setReferences(allRefs);		
			try {
				synapseClient.getEntityHeaderBatch(list.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {					
						BatchResults<EntityHeader> headers;
						try {
							headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
							if (headers.getTotalNumberOfResults() == 1) {
								EntityHeader theHeader = headers.getResults().get(0);
								ownerObjectName = theHeader.getName();
								ownerObjectLink = DisplayUtils.getSynapseHistoryToken(theHeader.getId());
								refreshTableOfContents();
							}	
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {					
						view.showErrorMessage(caught.getMessage());
					}
				});
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
		}
	}
	
	public void clearState() {
		view.clear();
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	public void refreshTableOfContents() {
		synapseClient.getWikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					PaginatedResults<JSONEntity> wikiHeaders = nodeModelCreator.createPaginatedResults(results, WikiHeader.class);
					Map<String, TreeItem> wikiId2TreeItem = new HashMap<String, TreeItem>(); 
					
					//now grab all of the headers associated with this level
					for (JSONEntity headerEntity : wikiHeaders.getResults()) {
						WikiHeader header = (WikiHeader) headerEntity;
						boolean isCurrentPage = header.getId().equals(wikiKey.getWikiPageId());
						String href, title;
						if (header.getParentId() == null) {
							href = ownerObjectLink;
							title = ownerObjectName;
						}
						else {
							href = DisplayUtils.getSynapseWikiHistoryToken(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), header.getId());
							title = header.getTitle();
						}
						TreeItem item = new TreeItem(view.getHTML(href, title, isCurrentPage));
						wikiId2TreeItem.put(header.getId(), item);
					}
					//now set up the relationships
					TreeItem root = null;
					for (JSONEntity headerEntity : wikiHeaders.getResults()) {
						WikiHeader header = (WikiHeader) headerEntity;
						if (header.getParentId() != null){
							//add this as a child
							wikiId2TreeItem.get(header.getParentId()).addItem(wikiId2TreeItem.get(header.getId()));
						} else {
							root = wikiId2TreeItem.get(header.getId());
						}
					}
					
					//finally, expand all nodes to the current wiki page.
					if (root != null && wikiKey.getWikiPageId() != null) {
						TreeItem currentItem =  wikiId2TreeItem.get(wikiKey.getWikiPageId());
						while(currentItem != null) {
							currentItem.setState(true);
							currentItem = currentItem.getParentItem();
						}
					}
					
					view.configure(root);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//if this is because the wiki header root was not found, and the parent wiki id is null,
				if (caught instanceof NotFoundException) {
					//do nothing, show nothing
					view.clear();
				}
				else
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
