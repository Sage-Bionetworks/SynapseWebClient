package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowser implements PagesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private PagesBrowserView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private boolean canEdit;
	private WikiPageKey wikiKey; 
	private String ownerObjectName, ownerObjectLink;
	private WikiPageWidget.Callback callback;
	
	@Inject
	public PagesBrowser(PagesBrowserView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		
		view.setPresenter(this);
	}	
	
	public void configure(final WikiPageKey wikiKey, final String ownerObjectName, final String ownerObjectLink, final String title, final boolean canEdit, WikiPageWidget.Callback callback) {
		this.canEdit = canEdit;
		this.wikiKey = wikiKey;
		this.callback = callback;
		this.ownerObjectName = ownerObjectName;
		this.ownerObjectLink = ownerObjectLink;
		//refresh the table of contents
		refreshTableOfContents();
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
		//if this is creating the root wiki, then refresh the full page
		final boolean isCreatingWiki = wikiKey.getWikiPageId() ==null;
		page.setParentWikiId(wikiKey.getWikiPageId());
		page.setTitle(name);
		String wikiPageJson;
		try {
			wikiPageJson = page.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createWikiPage(wikiKey.getOwnerObjectId(),  wikiKey.getOwnerObjectType(), wikiPageJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					if (isCreatingWiki) {
						view.showInfo("Wiki Created", "");
						callback.pageUpdated();
					}
					else
						view.showInfo("Page '" + name + "' Added", "");	
					refreshTableOfContents();
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
	
	public void refreshTableOfContents() {
		synapseClient.getWikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					PaginatedResults<JSONEntity> wikiHeaders = nodeModelCreator.createPaginatedResults(results, WikiHeader.class);
					Map<String, TreeItem> wikiId2TreeItem = new HashMap<String, TreeItem>(); 
					
					//now grab all of the headers associated with this level
					for (Iterator iterator = wikiHeaders.getResults().iterator(); iterator
							.hasNext();) {
						WikiHeader header = (WikiHeader) iterator.next();
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
					for (Iterator iterator = wikiHeaders.getResults().iterator(); iterator
							.hasNext();) {
						WikiHeader header = (WikiHeader) iterator.next();
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
					
					view.configure(canEdit, root);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//if this is because the wiki header root was not found, and the parent wiki id is null, and this user can edit the wiki then
				if (caught instanceof NotFoundException && canEdit) {
					//add the Create Wiki button
					view.configure(canEdit, null);
				}
				else
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
