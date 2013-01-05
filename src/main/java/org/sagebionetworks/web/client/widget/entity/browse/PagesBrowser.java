package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowser implements PagesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private PagesBrowserView view;
	private HandlerManager handlerManager = new HandlerManager(this);
	private String configuredEntityId;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private AutoGenFactory autogenFactory;
	private SearchServiceAsync searchService;
	
	@Inject
	public PagesBrowser(PagesBrowserView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			AdapterFactory adapterFactory, AutoGenFactory autogenFactory, SearchServiceAsync searchService) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		this.autogenFactory = autogenFactory;
		this.searchService = searchService;
		
		view.setPresenter(this);
	}	
	
	public void configure(final String entityId, final String title, boolean isProject) {
		if (isProject) {
			//find the root wiki folder
			searchService.searchEntities("entity", Arrays
					.asList(new WhereCondition[] { 
							new WhereCondition("parentId", WhereOperator.EQUALS, entityId), 
							new WhereCondition("name",WhereOperator.EQUALS, DisplayConstants.PROJECT_WIKI_FOLDER_NAME) 
							}), 1, 1, null,
					false, new AsyncCallback<List<String>>() {
					@Override
					public void onSuccess(List<String> result) {
						if (result == null || result.size() == 0) {
							//it wasn't found.  create the root wiki folder
							createRootWikiFolder(entityId);
						} else {
							//grab the wiki folder entity header
							try {
								EntityHeader wikiFolderHeader = nodeModelCreator.createJSONEntity(result.get(0), EntityHeader.class);
								configuredEntityId = wikiFolderHeader.getId();
								view.configure(wikiFolderHeader.getId(), title);
							} catch (JSONObjectAdapterException e) {
								onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
							}
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(caught.getMessage());
					}
				});					

			
		}
		else {
			//this is a Page, so the configured entityid is the given
			this.configuredEntityId = entityId;
			view.configure(configuredEntityId, title);
		}
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}
	
	@Override
	public void fireEntityUpdatedEvent() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}

	@Override
	public void createPage(final String name) {
		Entity folder = createNewEntity(Page.class.getName(), configuredEntityId);
		folder.setName(name);
		String entityJson;
		try {
			entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					view.showInfo("Page '" + name + "' Added", "");
					view.refreshTreeView(configuredEntityId);
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
	
	/*
	 * private methods
	 */
	
	private void createRootWikiFolder(String projectEntityId) {
		Entity folder = createNewEntity(Folder.class.getName(), projectEntityId);
		folder.setName(DisplayConstants.PROJECT_WIKI_FOLDER_NAME);
		String entityJson;
		try {
			entityJson = folder.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, true, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String newId) {
					configuredEntityId = newId;
					view.refreshTreeView(newId);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
				}			
			});
		} catch (JSONObjectAdapterException e) {			
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}

	private Entity createNewEntity(String className, String parentId) {
		Entity entity = (Entity) autogenFactory.newInstance(className);
		entity.setParentId(parentId);
		entity.setEntityType(className);		
		return entity;
	}

}
