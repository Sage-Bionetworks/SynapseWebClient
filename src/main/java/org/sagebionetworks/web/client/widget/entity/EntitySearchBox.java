package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget is a Synapse entity Id search box
 * 
 * @author Dburdick
 *
 */
public class EntitySearchBox implements EntitySearchBoxView.Presenter, IsWidget {
	
	private AdapterFactory adapterFactory;
	private EntitySearchBoxView view;
	private EntitySelectedHandler handler;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;

	private boolean retrieveVersions = false;
	
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public EntitySearchBox(AdapterFactory factory, EntitySearchBoxView view,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		super();		
		this.adapterFactory = factory;
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}

	/**
	 * Get widget with text box of specified width
	 * @param width the width of the input box
	 * @return
	 */	
	public Widget asWidget(int width) {
		this.view.build(width);
		return this.view.asWidget();
	}

	/**
	 * Default size
	 * @param width
	 * @return
	 */
	@Override
	public Widget asWidget() {
		return asWidget(300);
	}
	
	public void setEntitySelectedHandler(EntitySelectedHandler handler, boolean retrieveVersions) {
		this.handler = handler;		
		this.retrieveVersions = retrieveVersions;
	}

	@Override
	public void entitySelected(final String entityId, final String name) {
		if(handler != null) {
			List<EntityHeader> versions = null;
			if(retrieveVersions) {
				synapseClient.getEntityVersions(entityId, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<EntityHeader> versions = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
						handler.onSelected(entityId, name, versions.getResults());
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
					}
				});
			} else {				
				handler.onSelected(entityId, name, versions);
			}
		}
	}

	public interface EntitySelectedHandler {
		public void onSelected(String entityId, String name, List<EntityHeader> versions);
	}

	@Override
	public void search(String search) {
		SearchQuery query = DisplayUtils.getDefaultSearchQuery();
		
	}
	
	/**
	 * Clears out the state of the searchbox
	 */
	public void clearSelection() {
		this.view.clearSelection();
	}
	
	/*
	 * Private Methods
	 */
	private void executeSearch(SearchQuery query) { 								
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			query.writeToJSONObject(adapter);
			synapseClient.search(adapter.toJSONString(), new AsyncCallback<EntityWrapper>() {			
				@Override
				public void onSuccess(EntityWrapper result) {
					SearchResults currentResult = new SearchResults();		
					try {
						currentResult = nodeModelCreator.createEntity(result, SearchResults.class);
					} catch (RestServiceException e) {
						onFailure(null);					
					}									
					view.setSearchResults(currentResult);					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					}
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}
	}
	
}
