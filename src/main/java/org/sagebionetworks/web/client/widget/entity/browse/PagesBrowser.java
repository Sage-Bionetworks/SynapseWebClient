package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowser implements PagesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private PagesBrowserView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private boolean canEdit;
	private String parentWikiId,ownerId,ownerType; 
	
	@Inject
	public PagesBrowser(PagesBrowserView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		
		view.setPresenter(this);
	}	
	
	public void configure(final String ownerId, final String ownerType, final String title, final boolean canEdit, final String parentWikiId) {
		this.canEdit = canEdit;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
		this.parentWikiId = parentWikiId;
		//find the pages under this one
		refreshChildren();
	}
	
	public void clearState() {
		view.clear();
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}

	@Override
	public void createPage(final String name) {
		WikiPage page = new WikiPage();
		page.setParentWikiId(parentWikiId);
		page.setTitle(name);
		String wikiPageJson;
		try {
			wikiPageJson = page.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createWikiPage(ownerId,  ownerType, wikiPageJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					view.showInfo("Page '" + name + "' Added", "");
					refreshChildren();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}
	
		
	public void refreshChildren() {
		synapseClient.getWikiHeaderTree(ownerId, ownerType, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults wikiHeaders = nodeModelCreator.createJSONEntity(result, PaginatedResults.class);
					boolean rootLevelOnly = parentWikiId == null;
					List<WikiHeader> headersToDisplay = new ArrayList<WikiHeader>();
					//now grab all of the headers associated with this level
					for (Iterator iterator = wikiHeaders.getResults().iterator(); iterator
							.hasNext();) {
						WikiHeader header = (WikiHeader) iterator.next();
						if (rootLevelOnly) {
							//only include if the header parent is null too
							if (header.getParentId() == null)
								headersToDisplay.add(header);
						}
						else if (parentWikiId.equals(header.getParentId()))
							headersToDisplay.add(header);
					}
					view.configure(ownerId, ownerType, headersToDisplay, canEdit);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});				
	}


}
