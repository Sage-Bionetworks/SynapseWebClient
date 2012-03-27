package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.SearchParameters;
import org.sagebionetworks.web.shared.TableResults;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter, SynapseWidgetPresenter {
	
	private EntityTreeBrowserView view;
	private PlaceChanger placeChanger;
	private NodeServiceAsync nodeService;
	private SearchServiceAsync searchService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	
	private String currentSelection;
	
	private final int MAX_LIMIT = 200;
	
	@Inject
	public EntityTreeBrowser(EntityTreeBrowserView view, NodeServiceAsync nodeService, SearchServiceAsync searchService, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider, GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.nodeService = nodeService;
		this.searchService = searchService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		
		view.setPresenter(this);
	}	

	public void setRootEntities(List<EntityHeader> rootEntities) {
		this.view.setRootEntities(rootEntities);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
    
	@Override
	public PlaceChanger getPlaceChanger() {
		return placeChanger;
	}

	@Override
	public void setPlaceChanger(PlaceChanger placeChanger) {
		this.placeChanger = placeChanger;
	}

	@Override
	public void getFolderChildren(String entityId, final AsyncCallback<List<EntityHeader>> asyncCallback) {
		List<EntityHeader> headers = new ArrayList<EntityHeader>();		
		
		searchService.searchEntities("entity", Arrays
				.asList(new WhereCondition[] { new WhereCondition("parentId",
						WhereOperator.EQUALS, entityId) }), 1, MAX_LIMIT, null,
				false, new AsyncCallback<List<String>>() {
				@Override
				public void onSuccess(List<String> result) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for(String entityHeaderJson : result) {
						try {
							headers.add(nodeModelCreator.createEntity(entityHeaderJson, EntityHeader.class));
						} catch (RestServiceException e) {
							onFailure(e);
						}
					}
					asyncCallback.onSuccess(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, placeChanger, authenticationController.getLoggedInUser());				
					asyncCallback.onFailure(caught);
				}
			});					
	}

	@Override
	public void setSelection(String id) {
		currentSelection = id;
		fireEntitySelectedEvent();
	}	
	
	public String getSelected() {
		return currentSelection;
	}
		
	@SuppressWarnings("unchecked")
	public void addEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.addHandler(EntitySelectedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.removeHandler(EntitySelectedEvent.getType(), handler);
	}
	
	@Override
	public int getMaxLimit() {
		return MAX_LIMIT;
	}

	/*
	 * Private Methods
	 */
	private void fireEntitySelectedEvent() {
		handlerManager.fireEvent(new EntitySelectedEvent());
	}

	

}
